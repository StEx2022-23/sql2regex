package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;

import java.util.ArrayList;
import java.util.List;

class GroupByDeParserForRegExTest{

    @Test
    void testGroupByTwoStatements() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELCT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col1 , col2",
                "SELECT col1 GROUP BY col2,col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer, "SELECT col1 GROUP BY col1, col2", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatements() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2, col3",
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, true);
    }

    @Test
    void testGroupByThreeStatementsFailings() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1 col2, col3",
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 GROUP BY col1, col2, col3", toCheckedInput, false);
    }

    @Test
    void testGroupByThreeStatementsWithTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2", toCheckedInput, true);
    }

    @Test
    void testAliasSupport() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 t1 GROUP BY t1.col1, table1.col2"
        );
        TestUtils.validateListAgainstRegEx(new SettingsContainer(),"SELECT col1 FROM table1 t1 GROUP BY t1.col1, t1.col2", toCheckedInput, true);
    }

    @Test
    void testExpressionToStringListConvert(){
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<Expression> expressionList = new ArrayList<>();
        expressionList.add(new Column("col1"));
        expressionList.add(new Column("col2"));

        GroupByDeParserForRegEx groupByDeParserForRegEx = new GroupByDeParserForRegEx(new ExpressionDeParserForRegEx(defaultSettingsContainer), new StringBuilder(), defaultSettingsContainer);
        for(String str : groupByDeParserForRegEx.expressionListToStringList(expressionList)){
            Assertions.assertTrue(str.equals("\\s*(col1|ol1|cl1|co1|col)\\s*") || str.equals("\\s*(col2|ol2|cl2|co2|col)\\s*"));
        }


    }
}
