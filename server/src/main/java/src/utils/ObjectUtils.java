package src.utils;

import module.annotations.*;
import module.logic.exceptions.CannotCreateObjectException;
import module.logic.exceptions.FieldRestrictionException;
import module.logic.exceptions.NullFieldValueException;
import module.logic.streams.ConsoleInputManager;
import module.logic.streams.ConsoleOutputManager;
import module.logic.streams.InputManager;
import module.logic.streams.OutputManager;
import module.utils.BaseTypesRestrictions;
import module.utils.StringConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

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
