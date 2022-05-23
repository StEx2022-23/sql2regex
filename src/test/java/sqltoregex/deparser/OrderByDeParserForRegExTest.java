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

class OrderByDeParserForRegExTest {
    StringBuilder buffer = new StringBuilder();
    StatementDeParser statementDeParser = new StatementDeParserForRegEx(new ExpressionDeParserForRegEx(new SettingsManager()), buffer);

    OrderByDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    String getRegEx(String sampleSolution) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        return statementDeParser.getBuffer().toString();
    }

    void validateListAgainstRegEx(String sampleSolution, List<String> alternativeStatements, boolean isAssertTrue) throws JSQLParserException {
        String regex = this.getRegEx(sampleSolution);
        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

    @Test
    void testSetGetExpressionVisitor() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        SettingsManager settingsManager = new SettingsManager();
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParserForRegEx expressionDeParserForRegExOne = new ExpressionDeParserForRegEx(settingsManager);
        ExpressionDeParserForRegEx expressionDeParserForRegExTwo = new ExpressionDeParserForRegEx(settingsManager);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(expressionDeParserForRegExOne, buffer, settingsManager);
        orderByDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegExTwo);
        Assertions.assertEquals(expressionDeParserForRegExTwo, orderByDeParserForRegEx.getExpressionVisitor());
    }

    @Test
    void testSimpleOrderByAsc() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC"
        );
        validateListAgainstRegEx("SELECT col1, col2 FROM table1 ORDER BY col1 ASC", toCheckedInput, true);
    }

    @Test
    void testSimpleOrderByDesc() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC"
        );
        validateListAgainstRegEx("SELECT col1, col2 FROM table1 ORDER BY col1 DESC", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderBy() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 DESC",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  ASC"
        );
        validateListAgainstRegEx("SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithNullFirstLast() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULS LAST, col2 AC NULS FIRST"
        );
        validateListAgainstRegEx("SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithIsSibling() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLINGS  BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER  SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLNGS BY col1 DESC NULS LAST, col2 AC NULS FIRST"
        );
        validateListAgainstRegEx("SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST", toCheckedInput, true);
    }


}
