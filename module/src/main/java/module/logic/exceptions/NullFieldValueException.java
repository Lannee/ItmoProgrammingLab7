package module.logic.exceptions;

public class NullFieldValueException extends NumberFormatException {
    public NullFieldValueException() {
        this("Field value cannot be null");
    }

    public NullFieldValueException(String s) {
        super(s);
    }
}
