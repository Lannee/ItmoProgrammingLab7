package src.commands;

import module.commands.CommandDescription;
import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.connection.requestModule.RequestFactory;
import module.connection.requestModule.TypeOfRequest;
import module.connection.responseModule.CommandsDescriptionResponse;
import module.connection.responseModule.Response;
import module.logic.exceptions.InvalidResponseException;

import java.util.LinkedList;
import java.util.List;

public class CommandsHandler {
    private static List<CommandDescription> commandsForLoggedUsers = new LinkedList<>();
    private static List<CommandDescription> commandsForUnloggedUsers = new LinkedList<>();

    private final IConnection connection;

    public CommandsHandler(IConnection connection) {
        this.connection = connection;
    }

    public void initializeCommands() throws InvalidResponseException {
        Request request = RequestFactory.createRequest(TypeOfRequest.INITIALIZATION);
        connection.send(connection.getRecipientHost(), connection.getRecipientPort(), request);
        Response response = (Response) connection.packetConsumer();
//        Response response = connection.sendRequestGetResponse(request);
        if(!(response instanceof CommandsDescriptionResponse commandsDescriptionResponse)) throw new InvalidResponseException();
        commandsForLoggedUsers = commandsDescriptionResponse.getCommandsForLoggedUsers();
        commandsForUnloggedUsers = commandsDescriptionResponse.getCommandsForUnloggedUsers();
        System.out.println(commandsForLoggedUsers);
    }

    public CommandDescription getCommandDescriptionForLoggedUsers(String commandName) {
        for(CommandDescription commandDescription : commandsForLoggedUsers) {
            if(commandDescription.getCommandName().equals(commandName))
                return commandDescription;
        }
        return null;
    }

    public CommandDescription getCommandDescriptionForUnloggedUsers(String commandName) {
        for(CommandDescription commandDescription : commandsForUnloggedUsers) {
            if(commandDescription.getCommandName().equals(commandName))
                return commandDescription;
        }
        return null;
    }
}
