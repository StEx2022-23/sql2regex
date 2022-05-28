package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StatementDeParserForRegExTest extends UserSettingsPreparer {
    StringBuilder buffer = new StringBuilder();
    StatementDeParser statementDeParser;

    StatementDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.ALL);
        this.statementDeParser = new StatementDeParserForRegEx(new ExpressionDeParserForRegEx(this.settingsManager),
                                                               buffer, this.settingsManager);
    }

    boolean checkAgainstRegEx(String regex, String toChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toChecked);
        return matcher.matches();
    }

    String getRegEx(String sampleSolution) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        return statementDeParser.getBuffer().toString();
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

    void validateListAgainstRegEx(String sampleSolution, List<String> alternativeStatements,
                                  boolean isAssertTrue) throws JSQLParserException {
        String regex = this.getRegEx(sampleSolution);
        for (String str : alternativeStatements) {
            if (isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

    @Test
    void withClause() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1"
        );
        validateListAgainstRegEx(
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1",
                toCheckedInput, false);
    }
}
