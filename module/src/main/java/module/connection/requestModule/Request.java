package module.connection.requestModule;

import java.io.Serializable;
import java.util.Arrays;

public class Request implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private String commandName;
    private Object[] argumentsToCommand;
    private TypeOfRequest typeOfRequest;
    private String userName;
    private String userPassword;

    public Request (String CommandName, Object[] ArgumentsToCommand, TypeOfRequest TypeOfRequest, String userName, String userPassword) {
        this.commandName = CommandName;
        this.argumentsToCommand = ArgumentsToCommand;
        this.typeOfRequest = TypeOfRequest;
        this.userName = userName;
        this.userPassword = userPassword;
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

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
