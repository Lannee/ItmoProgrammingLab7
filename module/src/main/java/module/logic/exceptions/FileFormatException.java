package module.logic.exceptions;

import java.io.IOException;

/**
 * Says that file has invalid format
 */
public class FileFormatException extends IOException {
    public FileFormatException() {
        super();
    }

    public FileFormatException(String message) {
        super(message);
    }

    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileFormatException(Throwable cause) {
        super(cause);
    }
}
