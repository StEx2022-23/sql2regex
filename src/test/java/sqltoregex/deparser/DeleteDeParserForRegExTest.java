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
    void testForImplementing(){
        final String sampleSolution = "DELETE tab as t1, tab FROM tab.col1 WHERE col2 > 1000.00";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DELETE tab.col1, tab.col2 FROM tab.col1 WHERE col2 > 1000.00"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).with(SettingsOption.COLUMNNAMESPELLING).with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }



}
