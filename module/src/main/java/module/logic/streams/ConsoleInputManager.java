package module.logic.streams;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Gets data from standard console input stream
 */
public class ConsoleInputManager extends InputManager {

    private final BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));
    private final Console console = System.console();

    @Override
    public synchronized String readLine(boolean maskLine) {
        String line;
        if((line = getNext()) == null) {
//            try {
//                line = reader.readLine();
//            } catch (IOException e) {}
            if(!maskLine)
                line = console.readLine();
            else
                line = new String(console.readPassword(""));
            if(line == null) {
                write("exit");
                return "";
            }
        }
        return line;
    }

    @Override
    public synchronized String readLine() {
        return readLine(false);
    }
}
