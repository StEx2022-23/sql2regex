package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;

public class DeleteDeParserForRegExTest {

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



}
