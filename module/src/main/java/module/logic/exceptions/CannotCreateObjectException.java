package module.logic.exceptions;

public class CannotCreateObjectException extends Exception {
    public CannotCreateObjectException(Throwable cause) {
        super("Unable to create object", cause);
    }

    public CannotCreateObjectException() {
        this("Unable to create object");
    }

    public CannotCreateObjectException(String message) {
        super(message);
    }

    public CannotCreateObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
