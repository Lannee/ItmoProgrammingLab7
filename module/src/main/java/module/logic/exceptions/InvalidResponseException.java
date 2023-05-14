package module.logic.exceptions;

public class InvalidResponseException extends Exception {

    public InvalidResponseException() {
        this("Invalid response");
    }

    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
