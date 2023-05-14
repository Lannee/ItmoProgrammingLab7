package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

/**
 * Outputs information about the collection to the standard output stream
 */
public class Info implements Command {
    public static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public Info(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) throws IllegalArgumentException {
        checkArgsConformity(args);
        return receiver.getInfo();
    }

    @Override
    public String getDescription() {
        return "Outputs information about the collection to the standard output stream";
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