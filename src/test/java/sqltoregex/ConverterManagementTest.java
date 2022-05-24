package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

class ConverterManagementTest {
    ConverterManagement converterManagement = new ConverterManagement();

    @Test
    void testStatementDeparsingWithoutValidation() throws JSQLParserException, XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Assertions.assertEquals(
                "^SELECT col1, col2 FROM table$",
                converterManagement.deparse("SELECT col1, col2 FROM table", false, false)
        );
        Assertions.assertEquals(
                "^col1 + col2$",
                converterManagement.deparse("col1+col2", true, false)
        );
    }

    @Test
    void testStatementDeparsingWithValidation() throws JSQLParserException, XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Assertions.assertEquals(
                "^SELECT col1, col2 FROM table$",
                converterManagement.deparse("SELECT col1, col2 FROM table", false, true)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            converterManagement.deparse("col1, col2", false, true)
        );
    }

    @Test
    void testExpressionDeparsing() throws JSQLParserException, XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Assertions.assertEquals(
                "^col1 + col2$",
                converterManagement.deparse("col1+col2", true, false)
        );
        Assertions.assertThrows(JSQLParserException.class, () ->
            converterManagement.deparse("SELECT col1, col2 FROM table", true, false)
        );
    }

    @Test
    void testValidation() {
        Assertions.assertEquals(true, converterManagement.validate("SELECT col1, col2 FROM table"));
        Assertions.assertEquals(false, converterManagement.validate("col2 FROM table"));
    }
}
