package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

class SelectDeParserForRegExTest {
    StringBuilder buffer = new StringBuilder();

    @Test
    void SelectDeparserIsWorking() throws JSQLParserException, XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Statement statement = CCJSqlParserUtil.parse("SELECT col1, col2 FROM Test");
        StatementDeParser defaultStatementDeparser = new StatementDeParserForRegEx(new ExpressionDeParserForRegEx(new SettingsManager()), buffer);
        statement.accept(defaultStatementDeparser);
        String output = defaultStatementDeparser.getBuffer().toString().replace("\\", "\\\\");
        Assertions.assertEquals("", output);
    }
}
