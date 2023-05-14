package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

/**
 * Removes an item from the collection by its id
 */
public class RemoveById implements Command {
    private static final CommandArgument[] args = {new CommandArgument("id", long.class)};
    public final static CommandType commandType = CommandType.LINE_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public RemoveById(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);

        try {
//            Long id = Long.parseLong(args[0]);
            Long id = (Long) args[0];
            Object obj = receiver.getElementByFieldValue(args()[0].getArgumentName(), id);
            if(obj != null) {
                receiver.removeOn(e -> e == obj, false);
                return "Object with " + args()[0] + " " + id + " was successfully removed";
            } else {
                return "Unable to remove element from the collection. No element with such " + args()[0];
            }
        } catch (NoSuchFieldException e) {
            return "Stored type does not support this command\n";
        } catch (NumberFormatException e) {
            return "Invalid command argument\n";
        }
    }

    @Override
    public String getDescription() {
        return "Removes an item from the collection by its " + args[0];
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