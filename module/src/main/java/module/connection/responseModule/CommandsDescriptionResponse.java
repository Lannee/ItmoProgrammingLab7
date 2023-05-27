package module.connection.responseModule;

import module.commands.CommandDescription;

import java.util.List;

public class CommandsDescriptionResponse extends Response {

    private final List<CommandDescription> loggedUsersCommandsDescriptions;
    private final List<CommandDescription> unloggedUsersCommandsDescriptions;

    public CommandsDescriptionResponse(List<CommandDescription> logedCommands, List<CommandDescription> unlogedCommands) {
        loggedUsersCommandsDescriptions = logedCommands;
        unloggedUsersCommandsDescriptions = logedCommands;
    }

    public List<CommandDescription> getCommandsForLogedUsers() {
        return loggedUsersCommandsDescriptions;
    }

    public List<CommandDescription> getCommandsForUnlogedUsers() {
        return unloggedUsersCommandsDescriptions;
    }

    @Override
    public String toString() {
        return "CommandsDescriptionResponse{" +
                "logedUsersCommandsDescriptions=" + loggedUsersCommandsDescriptions +
                ", unlogedUsersCommandsDescriptions=" + unloggedUsersCommandsDescriptions +
                '}';
    }
}
