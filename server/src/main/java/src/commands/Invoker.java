package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandDescription;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.Server;
import src.logic.data.Receiver;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Invoker {

    private final Map<String, Command> declaredClientCommands = new TreeMap<>();

    private final Map<String, Command> declaredServerCommands = new TreeMap<>();

    private final Receiver receiver;

    private final IConnection connection;

    private final Map<String, Integer> files = new HashMap<>();

    private Integer recursionDepth = 1;

    private static final Pattern ARG_PAT = Pattern.compile("\"[^\"]+\"|\\S+");

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public Invoker(IConnection connection, Receiver receiver) {
        this.connection = connection;
        this.receiver = receiver;
        declaredClientCommands.put("help", new Help(this));
        declaredClientCommands.put("info", new Info(receiver));
        declaredClientCommands.put("update", new Update(connection, receiver));
        declaredClientCommands.put("execute_script", new ExecuteScript(this));
        declaredClientCommands.put("add", new Add(receiver));
        declaredClientCommands.put("clear", new Clear(receiver));
        declaredClientCommands.put("exit", new Exit(receiver));
//        declaredClientCommands.put("save", new Save(receiver));
        declaredClientCommands.put("show", new Show(receiver));
        declaredClientCommands.put("remove_first", new RemoveFirst(receiver));
        declaredClientCommands.put("remove_head", new RemoveHead(receiver));
        declaredClientCommands.put("remove_by_id", new RemoveById(receiver));
        declaredClientCommands.put("print_ascending", new PrintAscending(receiver));
        declaredClientCommands.put("remove_greater", new RemoveGreater(receiver));
        declaredClientCommands.put("count_greater_than_weight", new CountGreaterThanWeight(receiver));
        declaredClientCommands.put("group_counting_by_id", new GroupCountingById(receiver));
        Command exitClient = new ExitClient();
        exitClient.setConnection(connection);
        declaredClientCommands.put("exit", exitClient);

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

    public String commandsInfo() {
        StringBuilder out = new StringBuilder();
        declaredClientCommands.forEach((key, value) -> {
            out.append(key);
            if(value.args().length > 0) {
                String enteredByUserArguments = String.join(
                        ", ",
                        Arrays.stream(value.args()).
                                filter(CommandArgument::isEnteredByUser).
                                map(Object::toString).toArray(String[]::new)
                );

                String notEnteredByUserArguments = String.join(
                        ", ",
                        Arrays.stream(value.args()).
                                filter(e -> !e.isEnteredByUser()).
                                map(Object::toString).toArray(String[]::new)
                );

                if(!enteredByUserArguments.equals(""))
                    out.append(" ").append(enteredByUserArguments);
                if(!notEnteredByUserArguments.equals(""))
                    out.append(" {").append(notEnteredByUserArguments).append("}");
            }

            out.append(" : ").append(value.getDescription()).append("\n");
        });
        out.delete(out.toString().length() - 1, out.toString().length());
        return out.toString();
    }

    public String parseCommand(String line) {
        line = line.trim();
        if(line.equals("")) return "";

        String[] words = line.split(" ", 1);

        String command = words[0].toLowerCase();
        String[] args;
        if(words.length == 1)
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

    public String parseRequest(Request request) {
        return executeClientCommand(request.getCommandName(), request.getArgumentsToCommand());
    }

    public String executeClientCommand(String command, Object[] args) {
        if (declaredClientCommands.containsKey(command)) {
            logger.info("Command executing.");
            return declaredClientCommands.get(command).execute(args);
        } else {
            logger.error("Unknown command.");
            return "Unknown command " + command + ". Type help to get information about all commands.";
        }
    }

    private String[] parseArgs(String line) {
        return ARG_PAT.matcher(line)
                .results()
                .map(MatchResult::group)
                .map(e -> e.replaceAll("\"", ""))
                .toArray(String[]::new);
    }

    public List<CommandDescription> getCommandsDescriptions() {
        List<CommandDescription> commandDescriptions = new ArrayList<>(declaredClientCommands.size());
        declaredClientCommands.forEach((u, v) -> {
            commandDescriptions.add(
                    new CommandDescription(u, v.args(), v.getCommandType()));
        });
        return commandDescriptions;
    }
}