package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ConverterManagementTest {
    @Autowired
    MockMvc mvc;

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
