package module.logic.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Gets data from standard console input stream
 */
public class ConsoleInputManager extends InputManager {

    private final BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));

    @Override
    public String readLine() {
        String line;
        if((line = getNext()) == null) {
            try {
                line = reader.readLine();
            } catch (IOException e) {}

            if(line == null) {
                write("exit");
                return "";
            }
        }
        return line;
    }
}
