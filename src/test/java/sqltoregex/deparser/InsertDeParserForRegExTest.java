package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

class InsertDeParserForRegExTest{

    @Test
    void testInsertDeParserForRegExConstructor() {
        SettingsContainer settings = new SettingsContainer();
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(settings);
        Assertions.assertNotNull(insertDeParserForRegEx);
    }

    @Test
    void oneValueList() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col1, col2) VALUES ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('2', '1')",
                "INSERT INTO table (col2, col1) VALUE ('2', '1')"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO table (col1, col2) VALUES ('1', '2')", toCheckedInput, true);
    }

    @Test
    void oneValueListFailing() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col1, col2) VALUES ('2', '1')"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO table (col1, col2) VALUES ('1', '2')", toCheckedInput, false);
    }

    @Test
    void twoValueList() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')",
                "INSERT INTO table (col1, col2) VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUE ('22', '11'), ('2', '1')",
                "INSERT INTO table (col2, col1) VALUES (22, 11), (2, 1)"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')", toCheckedInput, true);
    }

    @Test
    void twoValueListFailing() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table (col2, col1) VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('22', '11'), ('1', '2')"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')", toCheckedInput, false);
    }

    @Test
    void insertWithSet() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO table SET name = 'Kim', isBFF = true"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO table SET name = 'Kim', isBFF = true", toCheckedInput, true);
    }

    @Test
    void testReturning() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "INSERT INTO t1 VALUES (val1, val2) RETURNING id"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"INSERT INTO t1 VALUES (val1, val2) RETURNING id", toCheckedInput, true);
    }
}
