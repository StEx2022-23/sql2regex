package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

class StatementDeParserForRegExTest {
    @Test
    void testConstructorOne(){
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        SettingsManager settingsManager = new SettingsManager();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
        StringBuilder buffer = new StringBuilder();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settingsManager);
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx, selectDeParserForRegEx, buffer);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorThree() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        SettingsManager settingsManager = new SettingsManager();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx, buffer);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }
}
