package module.logic.exceptions;

public class FileReadModeException extends Exception {

    public FileReadModeException() {
        this("Cannot read the file");
    }

    public FileReadModeException(String message) {
        super(message);
    }

    public FileReadModeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileReadModeException(Throwable cause) {
        super(cause);
    }
}
