package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

import java.util.Map;

/**
 * Groups the elements of the collection by the value of the id field, displays the number of elements in each group
 */
public class GroupCountingById implements Command {
    private static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public GroupCountingById(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);
        StringBuilder result = new StringBuilder();
        try {
            Map<Object, Integer> groups = receiver.groupByField("id");
            groups.forEach((u, v) -> result.append(u).append(" : ").append(v).append("\n"));
            result.deleteCharAt(result.length() - 1);
        } catch (NoSuchFieldException e) {
            return "Stored type does not support this command";
        }
        return result.toString();
    }

    @Override
    public String getDescription() {
        return "Groups the elements of the collection by the value of the id field, displays the number of elements in each group";
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