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
public class ConverterManagementTest {
    @Autowired
    MockMvc mvc;

    ConverterManagement converterManagement = new ConverterManagement();

    @Test
    void testValidation(){
        Assertions.assertEquals(converterManagement.validate("SELECT col1, col2 FROM table"), true);
        Assertions.assertEquals(converterManagement.validate("col2 FROM table"), false);
    }

    @Test
    void testDeparse() throws JSQLParserException {
        Assertions.assertEquals(
                converterManagement.deparse("SELECT col1, col2 FROM table"),
                "^SELECT col1, col2 FROM table$"
        );
    }
}
