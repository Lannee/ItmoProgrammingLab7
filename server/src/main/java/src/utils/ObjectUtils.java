package src.utils;

import module.logic.streams.InputManager;
import module.logic.streams.OutputManager;

/**
 * Util class for creating objects instances by their classes
 */
public class ObjectUtils {
    public static boolean agreement(InputManager in, OutputManager out, String phrase, boolean isReadingFromBuffer) {
        String answer;
        if (isReadingFromBuffer) {
            if (in.size() == 0)
                answer = "n";
            else
                answer = in.readLine();
        } else {
            out.print(phrase);
            answer = in.readLine();
        }

        switch (answer.trim().toLowerCase()) {
            case "y", "yes", "" -> {
                return true;
            }
            case "n", "no" -> {
                return false;
            }
            default -> {
                if (isReadingFromBuffer)
                    return false;
                return agreement(in, out, phrase, false);
            }
        }
    }
}
