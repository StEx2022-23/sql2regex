package sqltoregex.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

class SettingsFormTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptySql() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                ""
        ));
        Assertions.assertEquals(1, constraintViolations.size(),
                                "Form constraint validation should contain 1 violation");
        Assertions.assertEquals("sql",
                                constraintViolations.iterator().next().getPropertyPath().toString(),
                                "Property with constraint violation must be sql");
    }

    @Test
    void validFormCreation() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                "SQL STRING"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }

    @Test
    void validFormCreationAllBoxesUnchecked() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "SQL STRING"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }
}
