package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.connection.responseModule.*;
import module.stored.Dragon;
import src.logic.data.Receiver;
import src.utils.StringConverter;

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
        }
        try {
            // Long id = (Long) args[0];
            if (id <= 0)
                throw new NumberFormatException();
            Object obj = receiver.getElementByFieldValue(args()[0].getArgumentName(), id);
            if (obj != null) {
                    
                // System.out.println("object isn't null");
                // response = new CommandResponse("", ResponseStatus.WAITING);
                // connection.send(response);
                // System.out.println(
                //         "------------------------------Sended response waiting object------------------------------");
                // Request request = (Request) connection.handleByteArray(connection.receive());
                // System.out.println("------------------------------Recieved object------------------------------");
                // Object createdObject = request.getArgumentsToCommand()[1];
                // receiver.update(id, createdObject);
                // // receiver.removeFromCollection(obj);
                // // receiver.add(createdObject, id);

                return "WAITING";
            } else {
                // response = new CommandResponse("Element with this id does not exist",
                // ResponseStatus.FAILED);
                return "Element with this id does not exist";
            }
        } catch (NoSuchFieldException e) {
            return "Stored type does not support this command";
            // return "Stored type does not support this command\n";
        } catch (NumberFormatException nfe) {
            return "Incorrect argument value";
            // return "Incorrect argument value\n";
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