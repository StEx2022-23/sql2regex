package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExpressionDeParserForRegExTest {

    private void assertIsNonCapturingGroup(String regEx) {
        Assertions.assertTrue(regEx.contains("(?:"));
        Assertions.assertTrue(regEx.contains(")"));
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

    /**
     * Assertions.assertEquals("3\\s*\\s*2", b.toString());
     */
    @Test
    void binaryExpression() {
        final List<String> matching = new LinkedList<>();
        matching.add("3 / 2");
        matching.add("3/2");
        matching.add("3  /  2");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("3:2");
        testDeParsedExpressionVsStringLists("3 / 2", matching, notMatching);
        matching.clear();
        notMatching.clear();
        testDeParsedExpressionVsStringLists("4 DIV 2", matching, notMatching);
        testDeParsedExpressionVsStringLists("4 >> 2", matching, notMatching);
        testDeParsedExpressionVsStringLists("4 << 2", matching, notMatching);
    }

    /**
     * Assertions.assertEquals("1\\s+AND\\s+2|2\\s+AND\\s+1", b.toString());
     */
    @Test
    void commutativeBinaryExpression() {
        final List<String> matching = new LinkedList<>();
        matching.add("1 AND 2");
        matching.add("1   AND   2");
        matching.add("2 AND 1");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("1 UND 2");
        notMatching.add("1   &&   2");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("1 AND 2", matching, notMatching));
    }

    @Test
    void dateValue() {
        final List<String> matching = new LinkedList<>();
        matching.add("2022-05-17");
        matching.add("2022-5-17");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("17.05.2022");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("{d'2022-05-17'}", matching, notMatching));
    }

    @Test
    void exists() {
        final List<String> matching = new LinkedList<>();
        final List<String> notMatching = new LinkedList<>();
        matching.add("NOT EXISTS b");
        matching.add("NOT  EXISTS  b");
        testDeParsedExpressionVsStringLists("NOT EXISTS b", matching, notMatching);
    }

    @Test
    void fullTextSearch() {
        final List<String> matching = new LinkedList<>();
        matching.add("MATCH (col1,col2) AGAINST ('text' IN NATURAL LANGUAGE MODE)");
        matching.add("MATCH  (col1 , col2)  AGAINST  ( 'text' IN NATURAL LANGUAGE MODE)");
        matching.add("MATCH (col1,col2) AGAINST (\"text\" IN NATURAL LANGUAGE MODE)");
        final List<String> notMatching = new LinkedList<>();
        testDeParsedExpressionVsStringLists("MATCH (col1, col2) AGAINST ('text' IN NATURAL LANGUAGE MODE)", matching,
                                            notMatching);
    }

    @Test
    void inExpression() {
        final List<String> matching = new LinkedList<>();
        matching.add("2(+) NOT IN 5");
        matching.add("2(+)  NOT IN  5");
        final List<String> notMatching = new LinkedList<>();
        testDeParsedExpressionVsStringLists("2(+) NOT IN 5", matching, notMatching);
    }

    @Test
    void isBoolean() {
        final List<String> matching = new LinkedList<>();
        final List<String> notMatching = new LinkedList<>();
        matching.add("2 IS NOT TRUE");
        matching.add("2  IS  NOT  TRUE");
        notMatching.add("2 ISNOT TRUE");
        notMatching.add("2 IS NOTTRUE");
        testDeParsedExpressionVsStringLists("2 IS NOT TRUE", matching, notMatching);

        matching.clear();
        notMatching.clear();
        matching.add("2 IS NOT FALSE");
        matching.add("2  IS  NOT  FALSE");
        notMatching.add("2 ISNOT FALSE");
        notMatching.add("2 IS NOTFALSE");
        testDeParsedExpressionVsStringLists("2 IS NOT FALSE", matching, notMatching);
    }

    @Test
    void isNullExpression() {
        final List<String> matching = new LinkedList<>();
        final List<String> notMatching = new LinkedList<>();
        matching.add("2 ISNULL");
        matching.add("2 IS NULL");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("2 ISNULL", matching, notMatching));

        matching.clear();
        notMatching.clear();
        matching.add("2 NOT ISNULL");
        matching.add("2 NOT IS NULL");
        matching.add("2 IS NOT NULL");
        notMatching.add("2 IS NOTNULL");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("2 IS NOT NULL", matching, notMatching));
    }

    @Test
    void like() {
        final List<String> matching = new LinkedList<>();
        final List<String> notMatching = new LinkedList<>();
        matching.add("a NOT LIKE b");
        notMatching.add("a NOTLIKE b");
        testDeParsedExpressionVsStringLists("a NOT LIKE b", matching, notMatching);
    }

    @Test
    void minorThan() {
        final List<String> matching = new LinkedList<>();
        matching.add("5<8");
        matching.add("5< 8");
        matching.add("5< 8");
        matching.add("5 < 8");
        matching.add("8 > 5");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("5 > 8");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("5<8", matching, notMatching));
    }

    @Test
    void minorThanEquals() {
        final List<String> matching = new LinkedList<>();
        matching.add("5<=8");
        matching.add("5<= 8");
        matching.add("5<= 8");
        matching.add("5 <= 8");
        matching.add("8 >= 5");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("5 >= 8");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("5<=8", matching, notMatching));
    }

    @Test
    void multiplication() {
        final List<String> matching = new LinkedList<>();
        matching.add("5* 8");
        matching.add("5 *8");
        matching.add("8*5");
        final List<String> notMatching = new LinkedList<>();
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("5*8", matching, notMatching));
    }

    @Test
    void not() {
        final List<String> matching = new LinkedList<>();
        matching.add("NOT 5");
        matching.add("NOT  5");
        matching.add("!5");
        matching.add("!  5");
        final List<String> notMatching = new LinkedList<>();
        notMatching.add("NICHT 5");
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("NOT 5", matching, notMatching));
    }

    @Test
    void notEqualsTo() {
        final List<String> matching = new LinkedList<>();
        matching.add("5!=NULL");
        matching.add("NULL!=5");
        final List<String> notMatching = new LinkedList<>();
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("5!= NULL", matching, notMatching));
    }

    @Test
    void oldOracleJoinBinaryExpression() {
        final List<String> matching = new LinkedList<>();
        matching.add("2(+) = 5");
        matching.add("2(+)=5");
        matching.add("5 = 2(+)");
        matching.add("5=2(+)");
        final List<String> notMatching = new LinkedList<>();
        assertIsNonCapturingGroup(testDeParsedExpressionVsStringLists("2(+) = 5", matching, notMatching));
    }

    private String testDeParsedExpressionVsStringLists(String expressionString, List<String> matchingStrings,
                                                       List<String> notMatchingStrings) {
        StringBuilder b = new StringBuilder();
        SettingsContainer settings = new SettingsContainer();
        try {
            Expression expression = CCJSqlParserUtil.parseExpression(
                    expressionString);
            ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settings);
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
        } catch (JSQLParserException e) {
            Assertions.fail("Expression couldn't get parsed");
        }
        return b.toString();
    }

    @Test
    void testFullConstructor() {
        StringBuilder buffer = new StringBuilder();
        SettingsContainer settings = new SettingsContainer();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settings);
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(expressionDeParserForRegEx,
                                                                                      buffer, settings);
        ExpressionDeParserForRegEx expressionDeParserForRegExTwo = new ExpressionDeParserForRegEx(
                selectDeParserForRegEx, buffer, orderByDeParserForRegEx, settings);
        Assertions.assertNotNull(expressionDeParserForRegExTwo);
    }
}
