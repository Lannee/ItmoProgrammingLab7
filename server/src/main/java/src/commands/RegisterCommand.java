package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.authorization.Authorization;

public class RegisterCommand implements Command {

    public final static CommandArgument[] args = { new CommandArgument("login", String.class),
            new CommandArgument("password", String.class) };

    public final static CommandType commandType = CommandType.LINE_ARGUMENT_COMMAND;

    private Authorization authorization;

    public RegisterCommand(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);

        return authorization.registerUser((String) args[0], (String) args[1]).
                getDescription();
    }

    @Override
    public String getDescription() {
        return "Register user";
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
        throw new UnsupportedOperationException();
    }
}
