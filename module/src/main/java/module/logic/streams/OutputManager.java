package module.logic.streams;

/**
 * Controls data output
 */
public interface OutputManager {
    /**
     * Prints given string to OutputStream
     * @param message
     */
    void print(String message);
}
