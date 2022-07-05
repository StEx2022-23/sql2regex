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
    void checkValidation(){
        emptySql();
        invalidSql();
        validFormCreation();
        multipleSqlStatementsFormCreation();
        emptyMultipleSql();
        sqlStatementWithLinebreaks();
        validFormCreationAllBoxesUnchecked();
    }


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
