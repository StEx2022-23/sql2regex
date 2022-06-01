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

class StatementDeParserForRegExTest{

    @Test
    void testConstructorOne() {
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, new SettingsContainer());
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorThree() {
        SettingsContainer settings = new SettingsContainer();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            buffer, settings);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() {
        SettingsContainer settings = new SettingsContainer();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        StringBuilder buffer = new StringBuilder();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settings);
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            selectDeParserForRegEx,
                                                                                            buffer, settings);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void withClause() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(), "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1", toCheckedInput, false);
    }
}
