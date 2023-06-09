package src.commands;


import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;

/**
 * Exit program
 */
public class ExitClient implements Command {
    private static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;
    private IConnection connection;

    @Override
    public String execute(Object[] args, int userId) {
        checkArgsConformity(args);
        return "";
    }

    @Override
    public String getDescription() {
        return "Exit program in client.";
    }

    @Override
    public CommandArgument[] args() {
        return args;
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public void setConnection(IConnection connection) {
        this.connection = connection;
    }
}