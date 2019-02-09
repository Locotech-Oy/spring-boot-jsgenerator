package fi.locotech.jsgenerator.jsgenerator;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a field that tells the JSGenerator to override the Java type with something other.
 *
 * For example this could be used to substitute 'Date' with 'moment'.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JSType {
  String value() default "string";
}
