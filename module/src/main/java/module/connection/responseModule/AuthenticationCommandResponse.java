package module.connection.responseModule;

import java.util.List;

import module.commands.CommandDescription;

public class AuthenticationCommandResponse extends Response {

    private final IStatus responseStatus;

    public AuthenticationCommandResponse(IStatus responseStatus, List<CommandDescription> commandDescriptions) {
        this.responseStatus = responseStatus;
    }

    public IStatus getResponseStatus() {
        return responseStatus;
    }
}