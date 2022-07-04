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
                Collections.emptySet(),
                ""
        ));
        Assertions.assertEquals(1, constraintViolations.size(),
                                "Form constraint validation should contain 2 violations empty");
        Assertions.assertEquals("sql",
                                constraintViolations.iterator().next().getPropertyPath().toString(),
                                "Property with constraint violation must be sql");
    }

    @Test
    void invalidSql() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                "SELECT col1, col2 FROM"
        ));

        Assertions.assertEquals(1, constraintViolations.size(),
                                "Form constraint validation should contain 1 violation");
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
                Collections.emptySet(),
                "SELECT * FROM table1"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }

    @Test
    void multipleSqlStatementsFormCreation() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                "SELECT * FROM table1; \r\n SELECT * FROM table2 \r\n"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }

    @Test
    void emptyMultipleSql() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                "SELECT * FROM table1; \r\n SELECT * FROM table2; \r\n"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }

    @Test
    void sqlStatementWithLinebreaks() {
        Set<ConstraintViolation<SettingsForm>> constraintViolations = validator.validate(new SettingsForm(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                "SELECT * FROM table1; \r\n SELECT * FROM table2 \r\n"
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
                null,
                "SELECT * FROM table1"
        ));

        Assertions.assertTrue(constraintViolations.isEmpty(), "Form constraint validation should be empty");
    }
}
