package module.logic.exceptions;

public class FieldRestrictionException extends NumberFormatException {
    public FieldRestrictionException() {
        this("value is out of restriction bound");
    }

    public FieldRestrictionException(String s) {
        super(s);
    }
}
