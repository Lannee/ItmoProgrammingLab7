package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import module.stored.Dragon;
import src.logic.data.Receiver;
import module.logic.exceptions.CannotCreateObjectException;
import module.utils.ObjectUtils;

/**
 * Removes all items from the collection that exceed the specified
 */
public class RemoveGreater implements Command {
    private final Receiver receiver;
    private IConnection connection;

    private static final CommandArgument[] args = {new CommandArgument("element", Dragon.class, false)};
    public final static CommandType commandType = CommandType.OBJECT_ARGUMENT_COMMAND;

    public RemoveGreater(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
//        checkArgsConformity(args);
        Object obj = args[0];
        return receiver.removeOn(e -> e.compareTo(receiver.getStoredType().cast(obj)) > 0, false);
//        return "";
    }

    @Override
    public String getDescription() {
        return "Removes all items from the collection that exceed the specified";
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