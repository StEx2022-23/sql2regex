package sqltoregex.settings.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for validating by a list of methods by {@link AssertMethodAsTrueValidator}
 * src: http://soadev.blogspot.com/2010/01/jsr-303-bean-validation.html
 */
@Target(value = {TYPE, ANNOTATION_TYPE})
@Retention(value = RUNTIME)
@Documented
public @interface AssertMethodAsTrueList {
    AssertMethodAsTrue[] value() default {};
}
