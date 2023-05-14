package module.logic.streams;

import java.util.*;

/**
 * Controls data providing
 */
public abstract class InputManager {

//    private Queue<String> buffer = new LinkedList<>();
    private Deque<String> buffer = new LinkedList<>();

    public abstract String readLine();
    public void write(String line) {
        buffer.push(line);
    }

    public void write(Collection<String> lines) {
        lines.forEach(this::write);
    }

    protected String getNext() {
        return buffer.pollFirst();
    }

    public boolean isBufferEmpty() {
        return buffer.isEmpty();
    }

    public int size() {
        return buffer.size();
    }
}
