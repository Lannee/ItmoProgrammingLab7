package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import module.stored.Dragon;
import src.logic.data.Receiver;

/**
 * Updates the value of a collection item whose id is equal to the specified one
 */
public class Update implements Command {
    public final static CommandArgument[] args = { new CommandArgument("id", long.class),
            new CommandArgument("element", Dragon.class, false) };
    public final static CommandType commandType = CommandType.LINE_AND_OBJECT_ARGUMENT_COMMAND;

    private IConnection connection;

    private final Receiver receiver;

    public Update(IConnection connection, Receiver receiver) {
        this.receiver = receiver;
        this.connection = connection;
    }

    @Override
    public String execute(Object[] args, int userId) {
        // checkArgsConformity(args);
        Long id = (Long) args[0];

        if (args.length == 2) {
            Object createdObject = args[1];
            receiver.update(id, createdObject, userId);
            return "Object with " + args()[0].getArgumentName() + " " + id + " was successfully updated";
        } else {
            
            return receiver.update(id, userId);
        }

    }

    @Override
    public String getDescription() {
        return "Updates the value of a collection item whose " + args[0] + " is equal to the specified one";
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