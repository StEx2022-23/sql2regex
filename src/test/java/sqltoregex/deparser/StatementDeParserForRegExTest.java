package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

class StatementDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    StatementDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.ALL);
    }

    @Test
    void testConstructorOne() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        SettingsManager settingsManager = new SettingsManager();
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, settingsManager);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorThree() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        SettingsManager settingsManager = new SettingsManager();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            buffer, settingsManager);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        SettingsManager settingsManager = new SettingsManager();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
        StringBuilder buffer = new StringBuilder();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settingsManager);
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            selectDeParserForRegEx,
                                                                                            buffer, settingsManager);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void withClause() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1"
        );
        testUtils.validateListAgainstRegEx("WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1", toCheckedInput, false);
    }
}
