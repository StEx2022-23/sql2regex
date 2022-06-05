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



}
