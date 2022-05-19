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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SelectDeParserForRegExTest {
    StringBuilder buffer = new StringBuilder();
    StatementDeParser statementDeParser = new StatementDeParserForRegEx(new ExpressionDeParserForRegEx(new SettingsManager()), buffer);

    SelectDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    boolean checkAgainstRegEx(String regex, String toChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toChecked);
        return matcher.matches();
    }

    @Test
    void testSelectFromWithTwoColumns() throws JSQLParserException {
        String sampleSolution = "SELECT col1, col2 FROM table1";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELECT col2,col1 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELCT col2,col1 FROM table1",
                "SELECT col2, col1 FOM table1"
        );

        for(String str : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }
}
