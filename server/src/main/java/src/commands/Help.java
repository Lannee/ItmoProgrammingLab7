package src.commands;

import java.util.Map;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;

/**
 * Displays a list of all available commands
 */
public class Help implements Command {
    private static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;

    private final Invoker invoker;
    private IConnection connection;
    private final boolean isAuth;

    public Help(Invoker invoker, boolean isAuth) {
        this.invoker = invoker;
        this.isAuth = isAuth;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);
        return invoker.commandsInfo(isAuth);
//        return "";
    }

    @Override
    public CommandArgument[] args() {
        return args;
    }

    @Override
    public String getDescription() {
        return "Displays a list of all available commands";
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