package module.connection.responseModule;

public class CommandResponse extends Response {

    private final String stringResponse;

    private final ResponseStatus responseStatus;

    public CommandResponse(String response, ResponseStatus responseStatus) {
        stringResponse = response;
        this.responseStatus = responseStatus;
    }

    public CommandResponse(String response) {
        this(response, ResponseStatus.SUCCESSFULLY);
    }

    public CommandResponse ofString(String response) {
        return new CommandResponse(response);
    }

    public String getResponse() {
        return stringResponse;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }
}
