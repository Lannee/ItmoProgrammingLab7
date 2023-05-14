package src.commands;

import module.commands.CommandDescription;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.connection.requestModule.RequestFactory;
import module.connection.requestModule.TypeOfRequest;
import module.connection.responseModule.CommandsDescriptionResponse;
import module.connection.responseModule.Response;
import module.logic.exceptions.InvalidResponseException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CommandsHandler {
    private static List<CommandDescription> commands = new LinkedList<>();

    private final IConnection connection;

    public CommandsHandler(IConnection connection) {
        this.connection = connection;
    }

    public void initializeCommands() throws InvalidResponseException {
        Request request = RequestFactory.createRequest(TypeOfRequest.INITIALIZATION);
        connection.send(request);
        Response response = (Response) connection.receive();
//        Response response = connection.sendRequestGetResponse(request);
        if(!(response instanceof CommandsDescriptionResponse commandsDescriptionResponse)) throw new InvalidResponseException();
        commands = commandsDescriptionResponse.getCommands();
    }

    public CommandDescription getCommandDescription(String commandName) {
        for(CommandDescription commandDescription : commands) {
            if(commandDescription.getCommandName().equals(commandName))
                return commandDescription;
        }
        return null;
    }
}
