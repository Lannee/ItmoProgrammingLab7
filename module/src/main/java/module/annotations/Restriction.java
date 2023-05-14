package module.annotations;

import module.annotations.ValidationMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that show to object creator that field value must satisfy some restriction
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Restriction {
    ValidationMode filter();
    double value();
}
