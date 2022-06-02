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
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELCT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col1 , col2",
                "SELECT col1 GROUP BY col2,col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1 GROUP BY col1, col2",
                matchingMap,
                true
        );
    }

    @Test
    void testGroupByThreeStatements() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1, col2, col3",
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 GROUP BY col1, col2, col3",
                matchingMap,
                true
        );
    }

    @Test
    void testGroupByThreeStatementsFailings() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 GROUP BY col1 col2, col3",
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 GROUP BY col1, col2, col3",
                matchingMap,
                false
        );
    }

    @Test
    void testGroupByThreeStatementsWithTableNameAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
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
                new SettingsContainer(),
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2",
                matchingMap,
                true
        );
    }

    @Test
    void testExpressionToStringListConvert(){
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        List<Expression> expressionList = new ArrayList<>();
        expressionList.add(new Column("col1"));
        expressionList.add(new Column("col2"));

        GroupByDeParserForRegEx groupByDeParserForRegEx = new GroupByDeParserForRegEx(new ExpressionDeParserForRegEx(defaultSettingsContainer), new StringBuilder(), defaultSettingsContainer);
        for(String str : groupByDeParserForRegEx.expressionListToStringList(expressionList)){
            Assertions.assertTrue(str.equals("\\s*(col1|ol1|cl1|co1|col)\\s*") || str.equals("\\s*(col2|ol2|cl2|co2|col)\\s*"));
        }


    }
}
