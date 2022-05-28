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

class OrderByDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    OrderByDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.ALL);

    }

    @Test
    void testComplexerOrderByWithIsSibling() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLINGS  BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER  SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLNGS BY col1 DESC NULS LAST, col2 AC NULS FIRST"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                                 toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithNullFirstLast() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULS LAST, col2 AC NULS FIRST"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                                 toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithSynonyms() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER BY col1 absteigend, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 absteigend",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  aufsteigend"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC", toCheckedInput, true);
    }

    @Test
    void testSetGetExpressionVisitor() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        SettingsManager settingsManager = new SettingsManager();
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParserForRegEx expressionDeParserForRegExOne = new ExpressionDeParserForRegEx(settingsManager);
        ExpressionDeParserForRegEx expressionDeParserForRegExTwo = new ExpressionDeParserForRegEx(settingsManager);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(expressionDeParserForRegExOne,
                                                                                      buffer, settingsManager);
        orderByDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegExTwo);
        Assertions.assertEquals(expressionDeParserForRegExTwo, orderByDeParserForRegEx.getExpressionVisitor());
    }

    @Test
    void testSimpleOrderByAsc() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC"
        );
        testUtils.validateListAgainstRegEx("SELECT col1, col2 FROM table1 ORDER BY col1 ASC", toCheckedInput, true);
    }

    @Test
    void testSimpleOrderByDesc() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC"
        );
        testUtils.validateListAgainstRegEx("SELECT col1, col2 FROM table1 ORDER BY col1 DESC", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderBy() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 DESC",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  ASC"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 ORDER BY t1.col1, t1.col2"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 t1 ORDER BY t1.col1, t1.col2", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithAggregateFunction() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY SUMME(col1)"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 t1 ORDER BY SUM(col1)", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArguments() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1), col2",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUMME(col1)"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 t1 ORDER BY SUM(col1), col2", toCheckedInput, true);
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArgumentsAndTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1), t1.col2",
                "SELECT col1 FROM table1 t1 ORDER BY t1.col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUMME(col1)"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1), t1.col2", toCheckedInput, true);
    }


}
