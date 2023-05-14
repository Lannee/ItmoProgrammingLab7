package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.logic.data.Receiver;

/**
 * Print the number of elements whose weight field value is greater than the specified one
 */
public class CountGreaterThanWeight implements Command {
    private final static CommandArgument[] args = {new CommandArgument("weight", float.class)};
    public final static CommandType commandType = CommandType.LINE_ARGUMENT_COMMAND;

    private final Receiver receiver;
    private IConnection connection;

    public CountGreaterThanWeight(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);
        try {
            int amount = receiver.countCompareToValueByField(args()[0].getArgumentName(), (Comparable) args[0], (u, v) -> -u.compareTo(v));
            return Integer.toString(amount);
        } catch (NumberFormatException e) {
            return "Incorrect given value\n";
        } catch (NoSuchFieldException e) {
            return "Stored type does not have typed field\n";
        }
    }

    @Override
    public CommandArgument[] args() {
        return args;
    }

    @Override
    public String getDescription() {
        return "Print the number of elements whose " + args[0] + " field value is greater than the specified one";
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