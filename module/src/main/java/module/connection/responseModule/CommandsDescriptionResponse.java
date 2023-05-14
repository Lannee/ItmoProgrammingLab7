package module.connection.responseModule;

import module.commands.CommandDescription;

import java.util.List;

public class CommandsDescriptionResponse extends Response {

    private final List<CommandDescription> commandsDescriptions;

    public CommandsDescriptionResponse(List<CommandDescription> commands) {
        commandsDescriptions = commands;
    }

    public List<CommandDescription> getCommands() {
        return commandsDescriptions;
    }

    @Override
    public String toString() {
        return "CommandsDescriptionResponse{" +
                "commandsDescriptions=" + commandsDescriptions +
                '}';
    }
}
