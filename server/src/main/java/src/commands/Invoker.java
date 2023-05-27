package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandDescription;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.Server;
import src.authorization.Authorization;
import src.logic.data.Receiver;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Invoker {

    private final Map<String, Command> declaredAuthenticatedClientCommands = new TreeMap<>();
    private final Map<String, Command> declaredNonAuthenticatedClientCommands = new TreeMap<>();
    private final Map<String, Command> declaredServerCommands = new TreeMap<>();

    private final Receiver receiver;

    private final IConnection connection;

    private final Map<String, Integer> files = new HashMap<>();

    private Integer recursionDepth = 1;

    private static final Pattern ARG_PAT = Pattern.compile("\"[^\"]+\"|\\S+");

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public Invoker(IConnection connection, Authorization authorization, Receiver receiver) {
        this.connection = connection;
        this.receiver = receiver;
        declaredAuthenticatedClientCommands.put("help", new Help(this, true));
        declaredAuthenticatedClientCommands.put("info", new Info(receiver));
        declaredAuthenticatedClientCommands.put("update", new Update(connection, receiver));
        declaredAuthenticatedClientCommands.put("execute_script", new ExecuteScript(this));
        declaredAuthenticatedClientCommands.put("add", new Add(receiver));
        declaredAuthenticatedClientCommands.put("clear", new Clear(receiver));
        declaredAuthenticatedClientCommands.put("exit", new Exit(receiver));
        // declaredClientCommands.put("save", new Save(receiver));
        declaredAuthenticatedClientCommands.put("show", new Show(receiver));
        declaredAuthenticatedClientCommands.put("remove_first", new RemoveFirst(receiver));
        declaredAuthenticatedClientCommands.put("remove_head", new RemoveHead(receiver));
        declaredAuthenticatedClientCommands.put("remove_by_id", new RemoveById(receiver));
        declaredAuthenticatedClientCommands.put("print_ascending", new PrintAscending(receiver));
        declaredAuthenticatedClientCommands.put("remove_greater", new RemoveGreater(receiver));
        declaredAuthenticatedClientCommands.put("count_greater_than_weight", new CountGreaterThanWeight(receiver));
        declaredAuthenticatedClientCommands.put("group_counting_by_id", new GroupCountingById(receiver));
        Command exitClient = new ExitClient();
        exitClient.setConnection(connection);
        declaredAuthenticatedClientCommands.put("exit", exitClient);

        declaredNonAuthenticatedClientCommands.put("help", new Help(this, false));
        declaredNonAuthenticatedClientCommands.put("login", new LoginCommand(authorization));
        declaredNonAuthenticatedClientCommands.put("register", new RegisterCommand(authorization));
        declaredNonAuthenticatedClientCommands.put("exit", exitClient);

        declaredServerCommands.put("exit", new Exit(receiver));
        declaredServerCommands.put("save", new Save(receiver));

        logger.info("Invoker initialized.");
    }

    public int getRecursionSize() {
        return files.size();
    }

    public void clearRecursion() {
        files.clear();
    }

    // Holy sh. NEEDED TO BE FIXED
    public synchronized String commandsInfo(boolean isAuth) {
        StringBuilder out = new StringBuilder();
        if (isAuth) {
            declaredAuthenticatedClientCommands.forEach((key, value) -> {
                out.append(key);
                if (value.args().length > 0) {
                    String enteredByUserArguments = String.join(
                            ", ",
                            Arrays.stream(value.args()).filter(CommandArgument::isEnteredByUser).map(Object::toString)
                                    .toArray(String[]::new));

                    String notEnteredByUserArguments = String.join(
                            ", ",
                            Arrays.stream(value.args()).filter(e -> !e.isEnteredByUser()).map(Object::toString)
                                    .toArray(String[]::new));

                    if (!enteredByUserArguments.equals(""))
                        out.append(" ").append(enteredByUserArguments);
                    if (!notEnteredByUserArguments.equals(""))
                        out.append(" {").append(notEnteredByUserArguments).append("}");
                }

                out.append(" : ").append(value.getDescription()).append("\n");
            });
        } else {
            declaredNonAuthenticatedClientCommands.forEach((key, value) -> {
                out.append(key);
                if (value.args().length > 0) {
                    String enteredByUserArguments = String.join(
                            ", ",
                            Arrays.stream(value.args()).filter(CommandArgument::isEnteredByUser).map(Object::toString)
                                    .toArray(String[]::new));

                    String notEnteredByUserArguments = String.join(
                            ", ",
                            Arrays.stream(value.args()).filter(e -> !e.isEnteredByUser()).map(Object::toString)
                                    .toArray(String[]::new));

                    if (!enteredByUserArguments.equals(""))
                        out.append(" ").append(enteredByUserArguments);
                    if (!notEnteredByUserArguments.equals(""))
                        out.append(" {").append(notEnteredByUserArguments).append("}");
                }

                out.append(" : ").append(value.getDescription()).append("\n");
            });
        }

        out.delete(out.toString().length() - 1, out.toString().length());
        return out.toString();
    }

    public synchronized String parseCommand(String line) {
        line = line.trim();
        if (line.equals(""))
            return "";

        String[] words = line.split(" ", 1);

        String command = words[0].toLowerCase();
        String[] args;
        if (words.length == 1)
            args = new String[0];
        else
            args = parseArgs(words[1]);
        logger.info("Command was parsed.");
        return executeServerCommand(command, args);
    }

    private String executeServerCommand(String command, String[] args) {
        if (declaredServerCommands.containsKey(command)) {
            logger.info("Command executing.");
            return declaredServerCommands.get(command).execute(args);
        } else {
            logger.error("Unknown command.");
            return "Unknown command " + command + ". Type help to get information about all commands.\n";
        }
    }

    public synchronized String parseRequest(Request request) {
        return executeClientCommand(request.getCommandName(), request.getArgumentsToCommand(), request.getUserName());
    }

    public synchronized String executeClientCommand(String command, Object[] args, String userName) {
        if (userName == null) {
            if (declaredNonAuthenticatedClientCommands.containsKey(command)) {
                logger.info("Command executing.");
                return declaredNonAuthenticatedClientCommands.get(command).execute(args);
            } else {
                logger.error("Unknown command.");
                return "Unknown command " + command + ". Type help to get information about all commands.";
            }
        } else {
            if (declaredNonAuthenticatedClientCommands.containsKey(command)) {
                logger.info("Command executing.");
                return declaredNonAuthenticatedClientCommands.get(command).execute(args);
            } 
            if (declaredAuthenticatedClientCommands.containsKey(command)) {
                logger.info("Command executing.");
                return declaredAuthenticatedClientCommands.get(command).execute(args);
            } else {
                logger.error("Unknown command.");
                return "Unknown command " + command + ". Type help to get information about all commands.";
            }
        }

    }

    private synchronized String[] parseArgs(String line) {
        return ARG_PAT.matcher(line)
                .results()
                .map(MatchResult::group)
                .map(e -> e.replaceAll("\"", ""))
                .toArray(String[]::new);
    }

    public synchronized List<CommandDescription> getCommandsDescriptions() {
        List<CommandDescription> commandDescriptions = new ArrayList<>(declaredAuthenticatedClientCommands.size());
        declaredAuthenticatedClientCommands.forEach((u, v) -> {
            commandDescriptions.add(
                    new CommandDescription(u, v.args(), v.getCommandType()));
        });
        return commandDescriptions;
    }

    public synchronized List<CommandDescription> getNonAuthenticatedCommandsDescription() {
        List<CommandDescription> commandDescriptions = new ArrayList<>(declaredNonAuthenticatedClientCommands.size());
        declaredNonAuthenticatedClientCommands.forEach((u, v) -> {
            commandDescriptions.add(
                    new CommandDescription(u, v.args(), v.getCommandType()));
        });
        return commandDescriptions;
    }
}