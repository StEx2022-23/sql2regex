package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.SettingsManager;
import sqltoregex.visitor.SelectVisitorJoinToWhere;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionDeParserForRegExTest {

    private final SettingsManager settingsManager = new SettingsManager();

    public ExpressionDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    @Test
    void commutativeBinaryExpression() throws JSQLParserException {
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "1 AND 2");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settingsManager);
        deParser.setBuffer(b);
        expression.accept(deParser);
        Assertions.assertEquals("1\\s+AND\\s+2|2\\s+AND\\s+1", b.toString());
    }

    @Test
    void binaryExpression() throws JSQLParserException{
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "3 / 2");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settingsManager);
        deParser.setBuffer(b);
        expression.accept(deParser);
        Assertions.assertEquals("3\\s*/\\s*2", b.toString());
    }

    @Test
    void between() throws JSQLParserException {
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "a BETWEEN 1 AND 5");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settingsManager);
        deParser.setBuffer(b);
        expression.accept(deParser);
        Assertions.assertEquals("a\\s+BETWEEN\\s+1\\s+AND\\s+5", b.toString());
    }

    @Test
    void dateValue() throws JSQLParserException {
        final List<String> testDates = new LinkedList<>();
        testDates.add("2022-05-17");
        testDates.add("2022-5-17");
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "{d'2022-05-17'}");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settingsManager);
        deParser.setBuffer(b);
        expression.accept(deParser);
        for (String testDate : testDates) {
            Pattern pattern = Pattern.compile(b.toString());
            Matcher matcher = pattern.matcher(testDate);
            Assertions.assertTrue(matcher.matches());
        }

    }
}
