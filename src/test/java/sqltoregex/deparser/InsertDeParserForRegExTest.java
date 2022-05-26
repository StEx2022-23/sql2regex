package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;

class InsertDeParserForRegExTest {
    TestUtils testUtils = new TestUtils(new SettingsManager());

    InsertDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    @Test
    void testInsertDeParserForRegExConstructor() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(new SettingsManager());
        Assertions.assertNotNull(insertDeParserForRegEx);
    }

    @Test
    void oneValueList() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col1, col2) VALUES ('1', '2')",
                "INSERT INTO table (col1, col2) VALUES ('2', '1')", //this should not match
                "INSERT INTO table (col2, col1) VALUES ('2', '1')",
                "INSERT INTO table (col2, col1) VALUE ('2', '1')"
        );
        testUtils.validateListAgainstRegEx("INSERT INTO table (col1, col2) VALUES ('1', '2')", toCheckedInput, true);
    }

    @Test
    void twoValueList() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')",
                "INSERT INTO table (col1, col2) VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('22', '11'), ('1', '2')", //this should not match
                "INSERT INTO table (col2, col1) VALUE ('22', '11'), ('1', '2')"
        );
        testUtils.validateListAgainstRegEx("INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')", toCheckedInput, true);
    }
}
