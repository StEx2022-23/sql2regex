package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

class GroupByDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    GroupByDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, URISyntaxException, SAXException {
        super(SettingsType.ALL);
    }

    @Test
    void testGroupByTwoStatements() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELCT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col1 , col2",
                "SELECT col1 GROUP BY col2,col1"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 GROUP BY col1, col2", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatements() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2, col3",
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatementsFailings() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1 col2, col3",
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, false);
    }

    @Test
    void testGroupByThreeStatementsWithTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2", toCheckedInput, true);
    }
}
