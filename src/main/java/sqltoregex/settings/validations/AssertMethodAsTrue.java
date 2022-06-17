package sqltoregex.settings.validations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for validating by method by {@link AssertMethodAsTrueValidator}
 * src: http://soadev.blogspot.com/2010/01/jsr-303-bean-validation.html
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {AssertMethodAsTrueValidator.class})
@Documented
public @interface AssertMethodAsTrue {
    String field() default "{value}";

    Class[] groups() default {};

    String message() default "{value} returned false";

    Class<? extends Payload>[] payload() default {};

    String value() default "isValid";
}