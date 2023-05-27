package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;
import src.authorization.Authorization;

public class LoginCommand implements Command {

    public final static CommandArgument[] args = { new CommandArgument("login", String.class, false),
            new CommandArgument("password", String.class, false) };

    public final static CommandType commandType = CommandType.AUTHENTICATION_COMMAND;

    private Authorization authorization;

    public LoginCommand(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);

        if(!authorization.isUserExists((String) args[0])) {
            return "User does not exists";
        }

        return authorization.loginUser((String) args[0], (String) args[1]).getDescription();
    }

    @Override
    public String getDescription() {
        return "Login user";
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
