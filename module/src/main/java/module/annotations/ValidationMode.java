package module.annotations;

import java.time.Instant;
import java.util.Date;
import java.util.function.BiPredicate;

public enum ValidationMode {
    TOP_NUMERIC_BOUND((u, v) -> {
        if(! (v instanceof Number && v instanceof Comparable)) throw new NumberFormatException();
        return Double.compare(u, ((Number) v).doubleValue()) >= 0;
    }),
    LOW_NUMERIC_BOUND((u, v) -> {
        if(! (v instanceof Number && v instanceof Comparable)) throw new NumberFormatException();
        return Double.compare(u, ((Number) v).doubleValue()) <= 0;
    }),
    MAX_STRING_LENGTH((u, v) -> {
        if(! (v instanceof CharSequence)) throw new NumberFormatException();
        return ((CharSequence) v).length() <= u;
    }),
    MIN_STRING_LENGTH((u, v) -> {
        if(! (v instanceof CharSequence)) throw new NumberFormatException();
        return ((CharSequence) v).length() >= u;
    }),

    MAX_DATE((u, v) -> {
        if(! (v instanceof Date)) throw new NumberFormatException();
        return ((Date) v).before(new Date(u.longValue()));
    }),

    MIN_DATE((u, v) -> {
        if(! (v instanceof Date)) throw new NumberFormatException();
        return ((Date) v).after(new Date(u.longValue() - 10801000));
    }),

    LOWER_THAN_CURRENT_DATE((u, v) -> {
        if(! (v instanceof Date)) throw new NumberFormatException();
        return ((Date) v).before(new Date());
    });

    BiPredicate<Double, Object> validation;

    ValidationMode(BiPredicate<Double, Object> validation) {
        this.validation = validation;
    }

    public BiPredicate<Double, Object> getValidation() {
        return validation;
    }
}
