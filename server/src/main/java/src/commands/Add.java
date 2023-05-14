package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import module.stored.Dragon;
import src.logic.data.Receiver;

/**
 * Add new element into collection
 */
public class Add implements Command {
    public final static CommandArgument[] args = {new CommandArgument("element", Dragon.class, false)};

    public final static CommandType commandType = CommandType.OBJECT_ARGUMENT_COMMAND;
    private final Receiver receiver;

    private IConnection connection;

    public Add(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
        receiver.add(args[0]);
        return "Object was successfully created";
    }

    @Override
    public String getDescription() {
        return "Add new element into collection";
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