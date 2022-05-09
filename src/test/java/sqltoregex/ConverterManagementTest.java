package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConverterManagementTest {
    ConverterManagement converterManagement = new ConverterManagement();

    @Test
    void testValidation(){
        Assertions.assertEquals(true, converterManagement.validate("SELECT col1, col2 FROM table"));
        Assertions.assertEquals(false, converterManagement.validate("col2 FROM table"));
    }

    @Test
    void testDeparse() throws JSQLParserException {
        Assertions.assertEquals(
                "^SELECT col1, col2 FROM table$",
                converterManagement.deparse("SELECT col1, col2 FROM table")
        );
    }
}
