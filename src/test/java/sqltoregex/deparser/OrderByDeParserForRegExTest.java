package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class OrderByDeParserForRegExTest{

    @Test
    void testComplexerOrderByWithIsSibling() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLINGS  BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER  SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER SIBLNGS BY col1 DESC NULS LAST, col2 AC NULS FIRST"
                )
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithNullFirstLast() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULS LAST, col2 AC NULS FIRST"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithSynonyms() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 absteigend, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 absteigend",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  aufsteigend"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                matchingMap,
                true
        );
    }

    @Test
    void testSetGetExpressionVisitor() {
        SettingsContainer settings = SettingsContainer.builder().build();
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParserForRegEx expressionDeParserForRegExOne = new ExpressionDeParserForRegEx(settings);
        ExpressionDeParserForRegEx expressionDeParserForRegExTwo = new ExpressionDeParserForRegEx(settings);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(expressionDeParserForRegExOne,
                                                                                      buffer, settings);
        orderByDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegExTwo);
        Assertions.assertEquals(expressionDeParserForRegExTwo, orderByDeParserForRegEx.getExpressionVisitor());
    }

    @Test
    void testSimpleOrderByAsc() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC",
                matchingMap,
                true
        );
    }

    @Test
    void testSimpleOrderByDesc() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderBy() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 DESC",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  ASC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithTableNameAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY t1.col1, t1.col2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 ORDER BY t1.col1, t1.col2",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithAggregateFunction() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1));",
                "SELECT col1 FROM table1 t1 ORDER BY SUMME(col1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1)",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArguments() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1));, col2",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUMME(col1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1), col2",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArgumentsAndTableNameAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1));, t1.col2",
                "SELECT col1 FROM table1 t1 ORDER BY t1.col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUMME(col1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1), t1.col2",
                matchingMap,
                true
        );
    }
}
