package src.utils;

import module.logic.exceptions.FieldRestrictionException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util class for suitable String parsing
 */
public class StringConverter {
    public final static Map<Class<?>, Function<String, ?>> methodForType = new HashMap<>();

    static {
        methodForType.put(float.class, e -> {
            Float f = Float.parseFloat(e);
            if(f.isInfinite()) throw new NumberFormatException();
            return f;
        });
        methodForType.put(Float.class, methodForType.get(float.class));
        methodForType.put(int.class, Integer::parseInt);
        methodForType.put(Integer.class, Integer::parseInt);
        methodForType.put(long.class, Long::parseLong);
        methodForType.put(Long.class, Long::parseLong);
        methodForType.put(Double.class, Double::parseDouble);
        methodForType.put(double.class, methodForType.get(Double.class));
        methodForType.put(String.class, e -> e);
        methodForType.put(Date.class, e -> {
//            Pattern pattern = Pattern.compile("^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$");
//            Pattern pattern = Pattern.compile("^((19|2[0-9])[7-9][0-9])-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$");
            Pattern pattern = Pattern.compile("^([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$");
            Matcher matcher = pattern.matcher(e);
            if(!matcher.find()) throw new NumberFormatException();
//            return new Date(Integer.parseInt(matcher.group(1)) - 1900, Integer.parseInt(matcher.group(3)) - 1, Integer.parseInt(matcher.group(4)));
            return new Date(Integer.parseInt(matcher.group(1)) - 1900, Integer.parseInt(matcher.group(2)) - 1, Integer.parseInt(matcher.group(3)));
        });
        methodForType.put(ZonedDateTime.class, e -> ZonedDateTime.of(LocalDateTime.parse(e), ZoneId.systemDefault()));
        methodForType.put(LocalDateTime.class, LocalDateTime::parse);
    }
}
