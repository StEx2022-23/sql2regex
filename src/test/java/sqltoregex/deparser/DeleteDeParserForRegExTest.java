package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class DeleteDeParserForRegExTest {

    @Test
    void testConstructorOne() {
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        Assertions.assertDoesNotThrow(() -> new DeleteDeParserForRegEx(settingsContainer));
        DeleteDeParserForRegEx deleteDeParserForRegEx = new DeleteDeParserForRegEx(settingsContainer);
        Assertions.assertNotNull(deleteDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() {
        StringBuilder buffer = new StringBuilder();
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settingsContainer);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(settingsContainer);
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(selectDeParserForRegEx, buffer, orderByDeParserForRegEx, settingsContainer);

        Assertions.assertDoesNotThrow(() -> new DeleteDeParserForRegEx(settingsContainer, expressionDeParserForRegEx, buffer));
        DeleteDeParserForRegEx deleteDeParserForRegEx = new DeleteDeParserForRegEx(settingsContainer, expressionDeParserForRegEx, buffer);
        Assertions.assertNotNull(deleteDeParserForRegEx);
    }

    @Test
    void testSetGetExpressionDeParserForRegExAreEqual(){
        StringBuilder buffer = new StringBuilder();
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settingsContainer);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(settingsContainer);
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(selectDeParserForRegEx, buffer, orderByDeParserForRegEx, settingsContainer);

        DeleteDeParserForRegEx deleteDeParserForRegEx = new DeleteDeParserForRegEx(settingsContainer);
        deleteDeParserForRegEx.setExpressionDeParserForRegEx(expressionDeParserForRegEx);

        Assertions.assertEquals(expressionDeParserForRegEx, deleteDeParserForRegEx.getExpressionDeParserForRegEx());
    }

    @Test
    void testDeleteTableOrder(){
        final String sampleSolution = "DELETE tab1, tab2 FROM tab3";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE tab1, tab2 FROM tab3"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "DELETE tab1, tab2 FROM tab3",
                "DELETE tab2, tab1 FROM tab3"
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
    void testAliasHandling(){
        final String sampleSolution = "DELETE tab1 t1, tab2 FROM tab3 t3";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE tab1 t1, tab2 FROM tab3 t3",
                "DELETE   tab1 t1  ,   tab2   FROM   tab3   t3",
                "DELETE tab1 AS t1, tab2 AS t2 FROM tab3 ALIAS t3"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testSimpleJoin(){
        final String sampleSolution = "DELETE FROM tab3 t3, tab4 t3";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "DELETE FROM tab3 t3, tab4 t3",
                "DELETE FROM tab4 t3, tab3 t3"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }
    @Test
    void testWhere(){
        final String sampleSolution = "DELETE FROM tab WHERE col1 = 1 AND col2 = 2";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE FROM tab WHERE col1 = 1 AND col2 = 2",
                "DELETE FROM tab WHERE col2 = 2 AND col1 = 1",
                "DELETE FROM tab WHERE 2 = col2 AND col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testJoin(){
        final String sampleSolution = "DELETE FROM tab1 INNER JOIN tab2 ON tab1.col1 = tab2.col2";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE FROM tab1 INNER JOIN tab2 ON tab1.col1 = tab2.col2",
                "DELETE FROM tab1 INNER JOIN tab2 ON tab2.col2 = tab1.col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testQuotationMarks(){
        final String sampleSolution = "DELETE FROM `tab1` INNER JOIN `tab2` ON `tab1`.`col1` = `tab2`.`col2`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE FROM tab1 INNER JOIN tab2 ON tab1.col1 = tab2.col2",
                "DELETE FROM tab1 INNER JOIN tab2 ON tab2.col2 = tab1.col1",
                "DELETE FROM \"tab1\" INNER JOIN \"tab2\" ON \"tab1\".\"col1\" = \"tab2\".\"col2\"",
                "DELETE FROM \"tab1\" INNER JOIN \"tab2\" ON \"tab2\".\"col2\" = \"tab1\".\"col1\""
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testQuotationMarksWithAlias(){
        final String sampleSolution = "DELETE FROM `tab1` `t1` WHERE `t1`.`col1`=2";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE FROM tab1 t1 WHERE t1.col1=2",
                "DELETE FROM `tab1` `t1` WHERE `t1`.`col1`=2",
                "DELETE FROM \"tab1\" \"t1\" WHERE \"t1\".\"col1\"=2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testDeleteAllNotation(){
        final String sampleSolution = "DELETE FROM table";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE FROM table",
                "DELETE * FROM table"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }
}
