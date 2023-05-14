package module.logic.streams;

/**
 * Sends data to standard console output stream
 */
public class ConsoleOutputManager implements OutputManager {
    @Override
    public void print(String message) {
        System.out.print(message);
    }
}
