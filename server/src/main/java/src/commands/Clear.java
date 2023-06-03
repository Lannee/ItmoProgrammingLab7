package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

/**
 * Clears the collection
 */
public class Clear implements Command {
    private final static CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public Clear(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args, int userId) {
        checkArgsConformity(args);
        int countOfRemoved = receiver.clear(userId);
        return "Successfully cleared " + countOfRemoved + ".\nYou can clear only objects you had created.";
    }

    @Override
    public CommandArgument[] args() {
        return args;
    }

    @Override
    public String getDescription() {
        return "Clears the collection";
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