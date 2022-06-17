package sqltoregex.settings.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * custom validator for validation by method
 * src: http://soadev.blogspot.com/2010/01/jsr-303-bean-validation.html enhanced with generic specification
 */
public class AssertMethodAsTrueValidator implements ConstraintValidator<AssertMethodAsTrue, Object> {
    private String methodName;
    private String fieldName;

    @Override
    public void initialize(AssertMethodAsTrue assertMethodAsTrue) {
        methodName = assertMethodAsTrue.value();
        fieldName = assertMethodAsTrue.field();
    }

    public boolean isValid(Object object,
                           ConstraintValidatorContext constraintValidatorContext) {

        try {
            Class<?> clazz = object.getClass();
            Method validate = clazz.getMethod(methodName);
            boolean isValid = (boolean) validate.invoke(object);
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(constraintValidatorContext
                                                                      .getDefaultConstraintMessageTemplate())
                        .addPropertyNode(fieldName).addConstraintViolation();
            }
            return isValid;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Error while validating SQL statement", e);
        }
        return false;
    }
}