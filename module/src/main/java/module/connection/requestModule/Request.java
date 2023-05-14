package module.connection.requestModule;

import java.io.Serializable;
import java.util.Arrays;

public class Request implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private String commandName;
    private Object[] argumentsToCommand;
    private TypeOfRequest typeOfRequest;

    public Request (String CommandName, Object[] ArgumentsToCommand, TypeOfRequest TypeOfRequest) {
        this.commandName = CommandName;
        this.argumentsToCommand = ArgumentsToCommand;
        this.typeOfRequest = TypeOfRequest;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object[] getArgumentsToCommand() {
        return argumentsToCommand;
    }

    public TypeOfRequest getTypeOfRequest() {
        return typeOfRequest;
    }

    @Override
    public String toString() {
        return "Request [commandName=" + commandName + ", argumentsToCommand=" + Arrays.toString(argumentsToCommand) + ", typeOfRequest="
                + typeOfRequest + "]";
    }
}
