package module.connection.responseModule;

import module.commands.CommandDescription;

import java.util.List;

public class CommandsDescriptionResponse extends Response {
    private static final long serialVersionUID = 6529685098263757690L;

    private final List<CommandDescription> loggedUsersCommandsDescriptions;
    private final List<CommandDescription> unloggedUsersCommandsDescriptions;

    public CommandsDescriptionResponse(List<CommandDescription> loggedCommands, List<CommandDescription> unloggedCommands) {
        loggedUsersCommandsDescriptions = loggedCommands;
        unloggedUsersCommandsDescriptions = unloggedCommands;
    }

    public List<CommandDescription> getCommandsForLoggedUsers() {
        return loggedUsersCommandsDescriptions;
    }

    public List<CommandDescription> getCommandsForUnloggedUsers() {
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
