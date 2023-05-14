package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

/**
 * Saves the collection to the file
 */
public class Save implements Command {
    private static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public Save(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);
        receiver.saveCollection();
        return "Collection was saved";
    }

    @Override
    public String getDescription() {
        return "Saves the collection to the file";
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