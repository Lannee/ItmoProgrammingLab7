package module.connection.responseModule;

public class LoggingResponse extends Response {

    private final boolean isUserExists;

    public LoggingResponse(boolean isUserExists) {
        this.isUserExists = isUserExists;
    }

    public boolean isUserExists() {
        return isUserExists;
    }
}
