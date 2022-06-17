package sqltoregex.settings.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;

/**
 * custom validator for validation by method
 * src: http://soadev.blogspot.com/2010/01/jsr-303-bean-validation.html enhanced with generic specification
 */
public class AssertMethodAsTrueValidator implements ConstraintValidator<AssertMethodAsTrue, Object> {
    private String methodName;
    private String fieldName;

    public void initialize(AssertMethodAsTrue assertMethodAsTrue) {
        methodName = assertMethodAsTrue.value();
        fieldName = assertMethodAsTrue.field();
    }

    public boolean isValid(Object object,
                           ConstraintValidatorContext constraintValidatorContext) {

        try {
            Class<?> clazz = object.getClass();
            Method validate = clazz.getMethod(methodName);
            Boolean isValid = (Boolean) validate.invoke(object);
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(constraintValidatorContext
                                                                      .getDefaultConstraintMessageTemplate())
                        .addPropertyNode(fieldName).addConstraintViolation();
            }
            return isValid;
        } catch (Throwable e) {
            System.err.println(e);
        }
        return false;
    }
}