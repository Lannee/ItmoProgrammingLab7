package module.commands;

import java.io.Serializable;

public class CommandArgument implements Serializable {

    private final static long serialVersionUID = 3381943064232312019L;

    private final String argumentName;
    private final Class<?> argumentType;
    private final boolean isEnteredByUser;

    public CommandArgument(String argumentName, Class<?> argumentType) {
        this(argumentName, argumentType, true);
    }

    public CommandArgument(String argumentName, Class<?> argumentType, boolean isEnteredByUser) {
        this.argumentName = argumentName;
        this.argumentType = argumentType;
        this.isEnteredByUser = isEnteredByUser;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public Class<?> getArgumentType() {
        return argumentType;
    }

    public boolean isEnteredByUser() {
        return isEnteredByUser;
    }

    @Override
    public String toString() {
        return argumentName;
    }
}