package module.connection.responseModule;

public class CommandConfirmationResponse extends Response {

    private final String message;
    private final ConfirmationStatus confirmationStatus;
    private final ResponseStatus responseStatus;

    public CommandConfirmationResponse(String message, ConfirmationStatus confirmationStatus, ResponseStatus responseStatus) {
        this.message = message;
        this.confirmationStatus = confirmationStatus;
        this.responseStatus = responseStatus;
    }

    public String getMessage() {
        return message;
    }

    public ConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
}
