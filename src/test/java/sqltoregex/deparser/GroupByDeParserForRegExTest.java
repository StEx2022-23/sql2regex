package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class GroupByDeParserForRegExTest{

    @Test
    void testGroupByTwoStatements() {
        final String sampleSolution = "SELECT col1 GROUP BY col1, col2";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col1 , col2"
        ));
        matchingMap.put(SettingsOption.GROUPBYELEMENTORDER, List.of(
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col2,col1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELCT col1 GROUP BY col1, col2"
        ));

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.GROUPBYELEMENTORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testGroupByThreeStatements() {
        final String sampleSolution = "SELECT col1 GROUP BY col1, col2, col3";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1, col2, col3"
        ));
        matchingMap.put(SettingsOption.GROUPBYELEMENTORDER, List.of(
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        ));

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.GROUPBYELEMENTORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testGroupByThreeStatementsFailings() {
        final String sampleSolution = "SELECT col1 GROUP BY col1, col2, col3";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1 col2, col3"
        ));
        matchingMap.put(SettingsOption.GROUPBYELEMENTORDER, List.of(
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                false
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.GROUPBYELEMENTORDER).build(),
                sampleSolution,
                matchingMap,
                false
        );
    }

    @Test
    void testGroupByThreeStatementsWithTableNameAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2",
                "SELECT col1 FROM table1 t1 GROUP BY table1.col1, t1.col2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2",
                matchingMap,
                true
        );
    }

    @Test
    void testAliasSupport() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, table1.col2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2",
                matchingMap,
                true
        );
    }

    @Test
    void testAliasSupportWithQuotationMark() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT `col1` FROM `table1` `t1` GROUP BY `t1`.`col1`, `table1`.`col2`",
                "SELECT \"col1\" FROM \"table1\" \"t1\" GROUP BY \"t1\".\"col1\", \"table1\".\"col2\""
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT `col1` FROM `table1` `t1` GROUP BY `t1`.`col1`, `t1`.`col2`",
                matchingMap,
                true
        );
    }

    @Test
    void testExpressionToStringListConvert(){
        List<Expression> expressionList = new ArrayList<>();
        expressionList.add(new Column("col1"));
        expressionList.add(new Column("col2"));

        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        GroupByDeParserForRegEx groupByDeParserForRegEx = new GroupByDeParserForRegEx(new ExpressionDeParserForRegEx(settingsContainer), new StringBuilder(), settingsContainer);
        for(String str : groupByDeParserForRegEx.expressionListToStringList(expressionList)){
            Assertions.assertTrue(str.equals("\\s*[`´'\"]?col1[`´'\"]?\\s*") || str.equals("\\s*[`´'\"]?col2[`´'\"]?\\s*"), str);
        }
    }
}
