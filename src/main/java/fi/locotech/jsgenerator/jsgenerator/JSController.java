package fi.locotech.jsgenerator.jsgenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in conjunction with {@link org.springframework.web.bind.annotation.RestController}
 * so that {@link JSGenerator} knows which controllers to transpile to JavaScript.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface JSController {
  Class value() default Object.class;
}
