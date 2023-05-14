package module.utils;

import module.logic.streams.ConsoleInputManager;
import module.logic.streams.ConsoleOutputManager;
import module.annotations.*;
import module.logic.exceptions.CannotCreateObjectException;
import module.logic.exceptions.FieldRestrictionException;
import module.logic.exceptions.NullFieldValueException;
import module.logic.streams.InputManager;
import module.logic.streams.OutputManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.time.LocalDate;
import java.util.function.Function;

/**
 * Util class for creating objects instances by their classes
 */
public class ObjectUtils {

    private final static InputManager in = new ConsoleInputManager();         // два потока ввода и вывода
    private final static OutputManager out = new ConsoleOutputManager();      // чтобы все работало пока без клиента
                                                                              // Исправить !!!!!!!!!
    public static final String nullValue = "null\u00A0";
    /**
     * Creates object with users interactive input
     * @param ClT - Class of constructing object
     * @return final object
     */
    public static <T> T createObjectInteractively(Class<T> ClT) throws CannotCreateObjectException {

        boolean isReadingFromBuffer = !in.isBufferEmpty();

        T obj = null;
        try {
            obj = ClT.getConstructor().newInstance();
        } catch (Exception ignored) { }

        Field[] fields = Arrays.stream(ClT
                        .getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Fillable.class))
                .toArray(Field[]::new);

        List<Field> fieldsList = Arrays.asList(fields);

        ListIterator<Field> iterator = fieldsList.listIterator();

        String line;
        while(iterator.hasNext()) {
            Field field = iterator.next();
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(Complex.class)) {

                    if (field.isAnnotationPresent(Nullable.class)) {
                        if (ObjectUtils.agreement(in, out, "Do you want to create " + fieldType.getSimpleName() + " object (y/n) : ", isReadingFromBuffer)) {
                            field.set(obj, createObjectInteractively(fieldType));
                        } else {
                            field.set(obj, null);
                        }
                    } else {
                        field.set(obj, createObjectInteractively(fieldType));
                    }

                } else if (fieldType.isEnum()) {
                    List<?> enumConstants = Arrays.asList(fieldType.getEnumConstants());

                    if(!isReadingFromBuffer) {
                        out.print("Enter " + field.getName() + "(");
                        out.print(String.join(", ", enumConstants.stream().map(Object::toString).toArray(String[]::new)) + ")");
                        out.print(field.isAnnotationPresent(Nullable.class) ? " (Null)" : "");
                        out.print(" : ");
                    }

                    if(isReadingFromBuffer && in.size() == 0) throw new CannotCreateObjectException("Invalid number of arguments");

                    line = in.readLine().toUpperCase();
                    if (line.equals("")) {
                        if (field.isAnnotationPresent(Nullable.class)) {
                            field.set(obj, null);
                        } else {
                            throw new NullFieldValueException();
                        }
                    } else {
                        Object enumValue;
                        try {
                            enumValue = Enum.valueOf((Class<Enum>) fieldType, line);
                        } catch (IllegalArgumentException iae) {
                            throw new NumberFormatException();
                        }
                        if (checkValueForRestrictions(field, enumValue))
                            field.set(obj, enumValue);
                        else
                            throw new FieldRestrictionException("Entered value has exceeded the allowed restrictions");
                    }
                } else {
                    if(!isReadingFromBuffer) {
                        out.print("Enter " + ClT.getSimpleName() + "'s " + field.getName());
                        if (fieldType.isInstance(new Date()))
                            out.print(" (in format year-month-day)");

                        String fieldRestrictions = getFieldRestrictions(field);
                        out.print(!fieldRestrictions.equals("") ? " (" + fieldRestrictions + ")" : "");
                        out.print(field.isAnnotationPresent(Nullable.class) ? " (Null)" : "");
                        out.print(" : ");
                    }

                    if(isReadingFromBuffer && in.size() == 0) throw new CannotCreateObjectException("Invalid number of arguments");

                    line = in.readLine();

                    if (line.equals("")) {
                        if (field.isAnnotationPresent(Nullable.class)) {
                            field.set(obj, null);
                        } else {
                            throw new NullFieldValueException();
                        }
                    } else {
                        Function<String, ?> convertFunction = StringConverter.methodForType.get(fieldType);
                        if (convertFunction == null) {
                            out.print("Sorry we don't know how to interpret " + ClT.getSimpleName() + "'s field " + field.getName() + " with " + fieldType.getSimpleName() + " type(\n");
                            field.set(obj, null);
                        } else {
                            Object value = convertFunction.apply(line);

                            if (checkValueForRestrictions(field, value))
                                field.set(obj, value);
                            else
                                throw new FieldRestrictionException("Entered value has exceeded the allowed restrictions");
                        }
                    }
                }
            } catch (NullFieldValueException | FieldRestrictionException e) {
                if(!isReadingFromBuffer) {
                    out.print(e.getMessage() + "\n");
                    iterator.previous();
                } else
                    throw new CannotCreateObjectException(e.getMessage(), e);
            } catch (NumberFormatException nfe) {
                if(!isReadingFromBuffer) {
                    out.print("Invalid value for field with " + fieldType.getSimpleName() + " type. Please try again\n");
                    iterator.previous();
                } else
                    throw new CannotCreateObjectException("Invalid value for field " + ClT.getSimpleName() + "." + field.getName() + " with " + fieldType.getSimpleName() + " type", nfe);
            } catch(CannotCreateObjectException ccoe) {
                throw new CannotCreateObjectException(ccoe.getMessage(), ccoe);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }

    public static boolean checkValueForRestrictions(Field field, Object value) {
        if(!field.isAnnotationPresent(Restriction.class)) return true;

        boolean out;

        Restriction restriction = field.getAnnotation(Restriction.class);
        out = restriction.filter().getValidation().test(restriction.value(), value);

        if(field.isAnnotationPresent(ExRestriction.class)) {
            ExRestriction exRestriction = field.getAnnotation(ExRestriction.class);
            Restriction exArgument = exRestriction.restriction();
            out &= exArgument.filter().getValidation().test(exArgument.value(), value);
        }

        return out;
    }

    private static String getFieldRestrictions(Field field) {
        Class<?> fieldType = field.getType();
        Double[] baseRestrictions = BaseTypesRestrictions.restrictions.get(fieldType);
        if(baseRestrictions == null) return "";

        String[] restrictions = Arrays.stream(baseRestrictions)
                .map(Object::toString)
                .toArray(String[]::new);

        if(field.getType().isInstance(new Date(0))) {
            restrictions[0] = Instant.ofEpochMilli(
                    Double.valueOf(
                            Double.parseDouble(restrictions[0])).longValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().toString();
            restrictions[1] = Instant.ofEpochMilli(
                            Double.valueOf(
                                    Double.parseDouble(restrictions[1])).longValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().toString();
        }

        if(field.isAnnotationPresent(Restriction.class)) {
            setRestrictions(field.getAnnotation(Restriction.class), restrictions, fieldType);
        }

        if(field.isAnnotationPresent(ExRestriction.class)) {
            setRestrictions(field.getAnnotation(ExRestriction.class).restriction(), restrictions, fieldType);
        }
        return (field.getType().isInstance("") ? "length: " : "") + String.join(", ", restrictions);
    }

    private static void setRestrictions(Restriction restriction, String[] restrictions, Class<?> fieldType) {
        switch (restriction.filter()) {
            case LOWER_THAN_CURRENT_DATE -> restrictions[1] = LocalDate.now().toString();
            case MAX_STRING_LENGTH, TOP_NUMERIC_BOUND -> restrictions[1] = String.valueOf(restriction.value());
            case LOW_NUMERIC_BOUND, MIN_STRING_LENGTH -> restrictions[0] = String.valueOf(restriction.value());
        }
    }

    public static void setFieldValue(Object o, String fieldName, Object value) throws NoSuchFieldException, IllegalArgumentException {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            field.set(o, value);
        } catch (IllegalAccessException e) { }
    }

    public static <T extends Comparable<T>> int saveCompare(T o1, T o2) {
        if(o1 == null & o2 == null)
            return 0;
        else if(o1 == null & o2 != null)
            return -1;
        else if(o1 != null & o2 == null)
            return 1;
        else
            return o1.compareTo(o2);
    }

    public static boolean agreement(InputManager in, OutputManager out, String phrase, boolean isReadingFromBuffer) {
        String answer;
        if(isReadingFromBuffer) {
            if(in.size() == 0)
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
                if(isReadingFromBuffer)
                    return false;
                return agreement(in, out, phrase, false);
            }
        }
    }

    public static String[] getHeaders(Class<?> cl, boolean showClassName) {
        List<String> headers = new LinkedList<String>();

        Field[] fields = getFieldsWithAnnotation(cl, Storable.class);

        StringBuilder header = new StringBuilder();
        for (Field field : fields) {
            if(showClassName)
                header.append(cl.getSimpleName()).append(".");
            header.append(field.getName());
            if (field.isAnnotationPresent(Complex.class)) {
                String[] exLevel = getHeaders(field.getType(), showClassName);
                for (String exHeader : exLevel) {
                    exHeader = header + "." + exHeader;
                    headers.add(exHeader);
                }
            } else {
//                headers.add(header.toString() + " " + field.getType().getName());
                headers.add(header.toString());
            }
            header.append(" ").append(field.getType());
            header.setLength(0);
        }

        return headers.toArray(String[]::new);
    }

    public static String[] getFieldsValues(Object obj) {
        Class<?> objCl = obj.getClass();
        List<String> values = new LinkedList<String>();

        Field[] fields = getFieldsWithAnnotation(objCl, Storable.class);

        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue;
            try {
                fieldValue = field.get(obj);
            } catch (IllegalAccessException ignore) {
                fieldValue = new Object();
            }

            if (field.isAnnotationPresent(Complex.class)) {
                String[] exLevel = new String[countObjectsFields(field.getType())];
                if (field.isAnnotationPresent(Nullable.class) && fieldValue == null) {
                    Arrays.fill(exLevel, nullValue);
                } else {
                    exLevel = getFieldsValues(fieldValue);
                }
                values.addAll(Arrays.asList(exLevel));
            } else {
                if (fieldValue == null) {
                    fieldValue = nullValue;
                } else {
                    if (field.getType().isInstance(new Date(0))) {
                        fieldValue = ((Date) fieldValue).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }
                    if (field.getType().isInstance(ZonedDateTime.now())) {
                        fieldValue = ((ZonedDateTime) fieldValue).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                    }
                }
                values.add(fieldValue.toString());
            }
        }

        return values.toArray(String[]::new);
    }

    public static Field[] getFieldsWithAnnotation(Class<?> cl, Class<? extends Annotation> annotation) {
        return Arrays.stream(cl
                        .getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(annotation))
                .toArray(Field[]::new);
    }

    private static Integer countObjectsFields(Class<?> cl) {
        Integer out = 0;
        Field[] fields = Arrays.stream(cl
                        .getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Storable.class))
                .toArray(Field[]::new);

        for(Field field : fields) {
            if(field.isAnnotationPresent(Complex.class)) {
                out += countObjectsFields(field.getType());
            } else {
                out++;
            }
        }

        return out;
    }
}
