package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConverterManagementTest {
    ConverterManagement converterManagement = new ConverterManagement();

    @Test
    void testStatementDeparsingWithoutValidation() throws JSQLParserException {
        Assertions.assertEquals(
                "^SELECT col1, col2 FROM table$",
                converterManagement.deparse("SELECT col1, col2 FROM table", Boolean.FALSE, Boolean.FALSE)
        );
        Assertions.assertEquals(
                "^col1 + col2$",
                converterManagement.deparse("col1+col2", Boolean.TRUE, Boolean.FALSE)
        );
    }

    @Test
    void testStatementDeparsingWithValidation() throws JSQLParserException {
        Assertions.assertEquals(
                "^SELECT col1, col2 FROM table$",
                converterManagement.deparse("SELECT col1, col2 FROM table", Boolean.FALSE, Boolean.TRUE)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            converterManagement.deparse("col1, col2", Boolean.FALSE, Boolean.TRUE)
        );
    }

    @Test
    void testExpressionDeparsing() throws JSQLParserException {
        Assertions.assertEquals(
                "^col1 + col2$",
                converterManagement.deparse("col1+col2", Boolean.TRUE, Boolean.FALSE)
        );
        Assertions.assertThrows(JSQLParserException.class, () ->
            converterManagement.deparse("SELECT col1, col2 FROM table", Boolean.TRUE, Boolean.FALSE)
        );
    }

    @Test
    void testValidation() {
        Assertions.assertEquals(true, converterManagement.validate("SELECT col1, col2 FROM table"));
        Assertions.assertEquals(false, converterManagement.validate("col2 FROM table"));
    }
}
