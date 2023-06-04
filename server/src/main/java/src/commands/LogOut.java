package src.commands;

import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.IConnection;

public class LogOut implements Command {
    public final static CommandArgument[] args = new CommandArgument[0];

    public final static CommandType commandType = CommandType.LOG_OUT_COMMAND;

    @Override
    public String execute(Object[] args, int userId) {
        return "log_out";
    }

    @Override
    public String getDescription() {
        return "Log out user";
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
