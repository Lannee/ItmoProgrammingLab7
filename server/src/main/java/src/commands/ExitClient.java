package src.commands;


import module.commands.CommandArgument;
import module.commands.CommandType;
import module.connection.DatagramConnection;
import module.connection.IConnection;
import module.connection.responseModule.CommandResponse;
import module.connection.responseModule.ResponseStatus;

/**
 * Exit program
 */
public class ExitClient implements Command {
    private static final CommandArgument[] args = new CommandArgument[0];
    public final static CommandType commandType = CommandType.NON_ARGUMENT_COMMAND;
    private IConnection connection;

    @Override
    public String execute(Object[] args) {
        checkArgsConformity(args);

        DatagramConnection connectionDC = (DatagramConnection) connection;
        connectionDC.send(new CommandResponse("", ResponseStatus.SUCCESSFULLY));
        connectionDC.setClientPort(null);
        connectionDC.setClientHost(null);
        return "";
    }

    @Override
    public String getDescription() {
        return "Exit program in client.";
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