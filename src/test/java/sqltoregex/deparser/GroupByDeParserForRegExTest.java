package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

class GroupByDeParserForRegExTest{

    @Test
    void testGroupByTwoStatements() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELCT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col1 , col2",
                "SELECT col1 GROUP BY col2,col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer, "SELECT col1 GROUP BY col1, col2", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatements() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2, col3",
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatementsFailings() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1 col2, col3",
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, false);
    }

    @Test
    void testGroupByThreeStatementsWithTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2", toCheckedInput, true);
    }

    @Test
    void testAliasSupport() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, table1.col2"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2", toCheckedInput, true);
    }
}
