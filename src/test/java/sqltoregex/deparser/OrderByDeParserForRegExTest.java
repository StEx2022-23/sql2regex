package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class OrderByDeParserForRegExTest{

    @Test
    void testComplexerOrderByWithIsSibling() {
        final String sampleSolution = "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1  ORDER  SIBLINGS    BY col1 DESC NULLS LAST,col2 ASC   NULLS FIRST"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                 "SELECT col1 FROM table1 ORDER SIBLINGS BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                 "SELECT col1 FROM table1 ORDER SIBLINGS BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                 "SELCT col1 FOM table1 ODER SIBINGS BY col1 DSC NULS LAT, col2 AC NULS FRST"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithNullFirstLast() {
        final String sampleSolution = "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT   col1   FROM table1   ORDER BY   col1 DESC   NULLS LAST  , col2 ASC NULLS FIRST"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC NULLS LAST, col2 ASC NULLS FIRST",
                "SELECT col1 FROM table1 ORDER BY col2 ASC NULLS FIRST, col1 DESC NULLS LAST"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testAscDescSynonyms() {
        final String sampleSolution = "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC";
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.OTHERSYNONYMS);
        stringSynonymGenerator.addSynonymFor("ASC", "aufsteigend");
        stringSynonymGenerator.addSynonymFor("DESC", "absteigend");

        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM    table1   ORDER BY    col1   DESC,   col2 ASC"
        ));
        matchingMap.put(SettingsOption.OTHERSYNONYMS, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col1 absteigend, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col1 absteigend, col2 aufsteigend",
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 aufsteigend"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 DESC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(stringSynonymGenerator).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
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
        orderByDeParserForRegEx.setExpressionDeParserForRegEx(expressionDeParserForRegExTwo);
        Assertions.assertEquals(expressionDeParserForRegExTwo, orderByDeParserForRegEx.getExpressionDeParserForRegEx());
    }

    @Test
    void testSimpleOrderByAsc() {
        final String sampleSolution = "SELECT col1, col2 FROM table1 ORDER BY col1 ASC";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 ASC",
                "SELECT  col2, col1 FROM table1 ORDER BY col1 ASC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testSimpleOrderByDesc() {
        final String sampleSolution = "SELECT col1, col2 FROM table1 ORDER BY col1 DESC";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1, col2 FROM table1 ORDER BY col1 DESC",
                "SELECT  col2, col1 FROM table1 ORDER BY col1 DESC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderBy() {
        final String sampleSolution = "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 ORDER BY col1 DESC, col2 ASC",
                "SELECT col1 FROM table1 ORDER BY col1  DESC , col2  ASC"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1 FROM table1 ORDER BY col2 ASC, col1 DESC"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
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
        final String sampleSolution = "SELECT col1 FROM table1 t1 ORDER BY SUM(col1)";
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor("SUM", "Summe");
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1)"
        ));
        matchingMap.put(SettingsOption.AGGREGATEFUNCTIONLANG, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUMME(col1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(stringSynonymGenerator).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArguments() {
        final String sampleSolution = "SELECT col1 FROM table1 t1 ORDER BY SUM(col1), col2";
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor("SUM", "Summe");
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(col1), col2"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUM(col1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithAggregateFunctionAndTwoArgumentsAndTableNameAlias() {
        final String sampleSolution = "SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1), t1.col2";
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor("SUM", "Summe");
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUM(t1.col1), t1.col2"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY t1.col2, SUM(col1)",
                "SELECT col1 FROM table1 t1 ORDER BY col2, SUM(col1)"
        ));
        matchingMap.put(SettingsOption.AGGREGATEFUNCTIONLANG, List.of(
                "SELECT col1 FROM table1 t1 ORDER BY SUMME(t1.col1), t1.col2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(stringSynonymGenerator).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexerOrderByWithTableNameAliasQuotationMark() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT `col1` FROM `table1` `t1` ORDER BY `t1`.`col1`, `t1.col2`",
                "SELECT \"col1\" FROM \"table1\" \"t1\" ORDER BY \"t1\".\"col1\", \"t1.col2\""
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT `col1` FROM `table1` `t1` ORDER BY `t1`.`col1`, `t1.col2`",
                matchingMap,
                true
        );
    }
}
