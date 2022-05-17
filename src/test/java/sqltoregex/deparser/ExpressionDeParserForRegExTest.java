package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

class ExpressionDeParserForRegExTest {

    private final SettingsManager settingsManager = new SettingsManager();

    public ExpressionDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    private void testDeParsedExpressionVsStringLists(String expressionString, List<String> matchingStrings, List<String> notMatchingStrings){
        try {
            Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                    expressionString);
            StringBuilder b = new StringBuilder();
            ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settingsManager);
            deParser.setBuffer(b);
            expression.accept(deParser);
            Pattern pattern = Pattern.compile(b.toString());
            for (String testString : matchingStrings) {
                Matcher matcher = pattern.matcher(testString);
                Assertions.assertTrue(matcher.matches(), "Pattern:" + b + " test String:" + testString);
            }
            for (String testString : notMatchingStrings) {
                Matcher matcher = pattern.matcher(testString);
                Assertions.assertFalse(matcher.matches(), "Pattern:" + b + " test String:" + testString);
            }
        }catch(JSQLParserException e){
            Assertions.fail("Expression couldn't get parsed");
        }
    }

    /**
     * Assertions.assertEquals("1\\s+AND\\s+2|2\\s+AND\\s+1", b.toString());
     */
    @Test
    void commutativeBinaryExpression(){
        final List<String> matching = new LinkedList<>();
        matching.add("1 AND 2");
        matching.add("1   AND   2");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("1 UND 2");
        notMatching.add("1   &&   2");
        testDeParsedExpressionVsStringLists("1 AND 2", matching, notMatching);
    }

    /**
     * Assertions.assertEquals("3\\s*\\s*2", b.toString());
     */
    @Test
    void binaryExpression(){
        final List<String> matching = new LinkedList<>();
        matching.add("3 / 2");
        matching.add("3/2");
        matching.add("3  /  2");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("3:2");
        testDeParsedExpressionVsStringLists("3 / 2", matching, notMatching);
    }

    /**
     * Assertions.assertEquals("a\\s+BETWEEN\\s+1\\s+AND\\s+5", b.toString());
     */
    @Test
    void between() {
        final List<String> matching = new LinkedList<>();
        matching.add("a BETWEEN 1 AND 5");
        matching.add(" a BETWEEN 1 AND 5 ");
        matching.add("a    BETWEEN   1   AND    5");
        final List<String> notMatching = new LinkedList<>();
        testDeParsedExpressionVsStringLists("a BETWEEN 1 AND 5", matching, notMatching);
    }

    @Test
    void dateValue(){
        final List<String> matching = new LinkedList<>();
        matching.add("2022-05-17");
        matching.add("2022-5-17");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("17.05.2022");
        testDeParsedExpressionVsStringLists("{d'2022-05-17'}", matching, notMatching);
    }
}
