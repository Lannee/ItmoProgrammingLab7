package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;

import java.util.Arrays;

/**
 * Command interface, sets the behavior for each team in the project
 */
public interface Command {
    String execute(Object[] args);

    String getDescription();

    CommandArgument[] args();

    CommandType getCommandType();

    void setConnection(IConnection connection);

    /**
     * Checks arguments matching
     * @param args1
     * @param args2
     */
    default void checkArgsConformity(Object[] args) {
        if(args.length != Arrays.stream(args()).filter(CommandArgument::isEnteredByUser).count()) throw new IllegalArgumentException("Invalid number of arguments\n");
    }

    static void checkArgsConformity(Object[] args1, Object[] args2) {
        if(args1.length != args2.length) throw new IllegalArgumentException("Invalid number of arguments");
    }
}
