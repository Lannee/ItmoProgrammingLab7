package module.commands;

import java.io.Serializable;
import java.util.Arrays;

public class CommandDescription implements Serializable {

    private final String commandName;
    private CommandArgument[] arguments;
    private final CommandType commandType;

    public CommandDescription(String commandName, CommandArgument[] arguments, CommandType commandType) {
        this.commandName = commandName;
        this.arguments = arguments;
        this.commandType = commandType;
    }

    public String getCommandName() {
        return commandName;
    }

    public CommandArgument[] getArguments() {
        return arguments;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public String toString() {
        return "CommandDescription{" +
                "commandName='" + commandName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", commandType=" + commandType +
                '}';
    }
}

