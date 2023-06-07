package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandDescription;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.connection.requestModule.RequestFactory;
import module.connection.requestModule.TypeOfRequest;
import module.connection.responseModule.CommandResponse;
import module.connection.responseModule.Response;
import module.connection.responseModule.ResponseStatus;
import module.logic.exceptions.CannotCreateObjectException;
import module.logic.exceptions.InvalidResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.Client;
import src.logic.authorization.SessionCash;
import src.logic.callers.ArgumentCaller;
import src.logic.callers.Callable;
import src.utils.StringConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Invoker {

    private final CommandsHandler commands;

    private final IConnection connection;

    private static final Pattern ARG_PAT = Pattern.compile("\"[^\"]+\"|\\S+");

    private static final Logger logger = LoggerFactory.getLogger(Invoker.class);

    private final Map<String, Integer> files = new HashMap<>();

    private Integer recursionDepth = 1;

    private Callable caller;

    private boolean isAuthed = false;

    public boolean isAuthed() {
        return isAuthed;
    }

    private String userName;
    private String userPassword;

    public Invoker(IConnection connection) throws IOException, InvalidResponseException {
        this.connection = connection;
        commands = new CommandsHandler(connection);
        commands.initializeCommands();
        logger.info("Invoker initialized.");
    }

    public String parseCommand(String line) throws NullPointerException {
        line = line.trim();
        if (line.equals(""))
            return "";

        caller = new ArgumentCaller();
        caller.handleCommand(line);
        String command = caller.getCommand();
        String[] args = caller.getArguments();
        String commandResult = validateCommand(command, args, userName, userPassword);
        return commandResult.equals("") ? "" : commandResult + "\n";
    }

    public String validateCommand(String commandName, String[] args, String userName, String userPassword)
            throws NullPointerException {
        CommandDescription commandDescription;
        if (isAuthed) {
            commandDescription = commands.getCommandDescriptionForLoggedUsers(commandName);
        } else {
            commandDescription = commands.getCommandDescriptionForUnloggedUsers(commandName);
        }
        if (commandDescription != null) {
            checkExit(commandName, args, userName, userPassword);
            CommandArgument[] arguments = commandDescription.getArguments();

            if (args.length != Arrays.stream(arguments).filter(CommandArgument::isEnteredByUser).count())
                return "Invalid number of arguments.";

            Object[] parsedArguments = new Object[args.length];

            for (int i = 0; i < arguments.length; i++) {
                CommandArgument argument = arguments[i];
                if (!argument.isEnteredByUser())
                    break;
                try {
                    parsedArguments[i] = StringConverter.methodForType.get(argument.getArgumentType()).apply(args[i]);
                } catch (NumberFormatException nfe) {
                    return "Invalid argument type";
                }
            }
            return formRequestAndGetResponse(commandName, parsedArguments, commandDescription, userName, userPassword);
        } else {
            logger.error("Unknown command '{}'. Type help to get information about all commands.", commandName);
            return "Unknown command " + commandName + ". Type help to get information about all commands.";
        }
    }

    public String formRequestAndGetResponse(String commandName, Object[] args, CommandDescription commandDescription,  String userName, String userPassword) throws NullPointerException {
        CommandResponse response;
        switch (commandDescription.getCommandType()) {
            case LINE_AND_OBJECT_ARGUMENT_COMMAND:
                response = sendRequestAndGetResponse(RequestFactory.createRequest(commandName, args, TypeOfRequest.CONFIRMATION, userName, userPassword));
                logger.info("Response with status '{}' received. Message - '{}'", response.getResponseStatus(), response.getResponse());
                if(response.getResponseStatus() == ResponseStatus.CONNECTION_REJECTED) {
                    return "Connection rejected. Server is working with another user";
                }
                if (response.getResponseStatus() != ResponseStatus.WAITING) {
                    return response.getResponse();
                }
            case OBJECT_ARGUMENT_COMMAND:
                // Getting array of arguments of command
                CommandArgument[] objectArguments = Arrays.stream(commandDescription.getArguments()).
                        filter(e -> !e.isEnteredByUser()).
                        toArray(CommandArgument[]::new);

                for(CommandArgument objectArgument : objectArguments) {
                    try {
                        args = addArgument(args, caller.getObjectArgument(objectArgument.getArgumentType()));
                        response = sendRequestAndGetResponse(RequestFactory.createRequest(commandName, args, TypeOfRequest.CONFIRMATION, userName, userPassword));
                        logger.info("Response with status '{}' received. Message - '{}'", response.getResponseStatus(), response.getResponse());
                        if(response.getResponseStatus() == ResponseStatus.CONNECTION_REJECTED) {
                            return "Connection rejected. Server is working with another user";
                        }
                        if (response.getResponseStatus() != ResponseStatus.WAITING) {
                            return response.getResponse();
                        }
                    } catch (CannotCreateObjectException e) {
                        logger.error("Cannot create object as argument to command with its type.");
//                        return "Error with creating object as argument to command.";
                        return e.getMessage();
                    }
                }
                response = sendRequestAndGetResponse(RequestFactory.createRequest(commandName, args, TypeOfRequest.CONFIRMATION, userName, userPassword));
                return response.getResponse();

            case SCRIPT_ARGUMENT_COMMAND:
                return execute_script((String) args[0]);
            case LOG_OUT_COMMAND:
                isAuthed = false;
                this.userName = null;
                this.userPassword = null;
                SessionCash.clearCash();
                return "You have successfully logged out";
            case LOG_IN_COMMAND:
                if(SessionCash.loadState()) {
                    this.userName = SessionCash.getUserName();
                    this.userPassword = SessionCash.getPassword();
                }
            case AUTHENTICATION_COMMAND:
                if(this.userName == null && this.userPassword == null) {
                    Client.out.print("Enter user name : ");
                    this.userName = Client.in.readLine().trim();
                    Client.out.print("Enter password : ");
                    this.userPassword = Client.in.readLine(true).trim();
                    if (this.userName.equals("") || this.userName.length() > 50) return "Invalid user name";
                    if (this.userPassword.equals("")) return "Password cannot be blank";
                }

                String[] newArgs = new String[2];
                newArgs[0] = this.userName;
                newArgs[1] = this.userPassword;
                response = sendRequestAndGetResponse(RequestFactory.createRequest(commandName, newArgs, TypeOfRequest.CONFIRMATION, null, null));

                if (response.getResponse().equals("Login successful") || response.getResponse().equals("Registration successful")) {
                    isAuthed = true;
                    SessionCash.saveSession(this.userName, this.userPassword);
                } else {
                    this.userName = null;
                    this.userPassword = null;
                    SessionCash.clearCash();
                }

                return this.userName != null
                        ?  response.getResponse() + "\nWelcome, " + this.userName + " !"
                        : response.getResponse();
            default:
                // If command is NON_ARGUMENT or LINE_ARGUMENT
                response = sendRequestAndGetResponse(RequestFactory.createRequest(commandName, args, TypeOfRequest.CONFIRMATION, userName, userPassword));
                logger.info("Response with status '{}' received. Message - '{}'", response.getResponseStatus(), response.getResponse());
                if(response.getResponseStatus() == ResponseStatus.CONNECTION_REJECTED) {
                    return "Connection rejected. Server is working with another user";
                }
                return response.getResponse();
        }
    }

    public void checkExit(String commandName, Object[] args, String userName, String userPassword) {
        if (commandName.equals("exit")) {
            CommandDescription commandDescription = commands.getCommandDescriptionForUnloggedUsers(commandName);
            formRequestAndGetResponse(commandName, args, commandDescription, userName, userPassword);
            System.exit(0);
        }
    }

    public CommandResponse sendRequestAndGetResponse(Request request) {
        connection.send(connection.getRecipientHost(), connection.getRecipientPort(), request);
        Response response = null;
        response = (Response) connection.packetConsumer();
        if (response instanceof CommandResponse commandResponse) {
            return commandResponse;
        }
        return null;
    }

    public static Object[] addArgument(Object[] args, Object obj) {
        int newArraySize = args.length + 1;
        Object[] newArray = new Object[newArraySize];
        System.arraycopy(args, 0, newArray, 0, args.length);
        newArray[args.length] = obj;
        return newArray;
    }

    private String[] parseArgs(String line) {
        return ARG_PAT.matcher(line)
                .results()
                .map(MatchResult::group)
                .map(e -> e.replaceAll("\"", ""))
                .toArray(String[]::new);
    }

    public String execute_script(String file) {
        if (!new File(file).exists()) {
            return "File \"" + file + "\" does not exist";
        }

        if (files.containsKey(file)) {
            Integer value = files.get(file);
            if (value >= recursionDepth) {
                files.clear();
                return "Recursion was cached. After executing file " + file + " " + recursionDepth + " times";
            }

            files.put(file, ++value);
        } else {
            files.put(file, 1);
            if (files.size() == 1) {
                int input = 0;
                do {
                    try {
                        Client.out.print("Please enter recursion depth (1, 50) : ");
                        input = Integer.parseInt(Client.in.readLine());
                    } catch (NumberFormatException ignored) {
                    }
                } while (input < 1 || input > 50);
                recursionDepth = input;
            }
        }

        try (InputStream fileInputStream = new FileInputStream(file);
                Reader decoder = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                BufferedReader lineReader = new BufferedReader(decoder)) {

            List<String> lines = new LinkedList<>();
            String line;
            while ((line = lineReader.readLine()) != null) {
                lines.add(line);
            }

            ListIterator<String> iterator = lines.listIterator(lines.size());

            while (iterator.hasPrevious()) {
                Client.in.write(iterator.previous());
            }

        } catch (IOException e) {
            return "Command cannot be executed: file " + file + " does not exist";
            // Client.out.print("Command cannot be executed: file " + file + " does not
            // exist.\n");
        }
        return "";
    }
}
