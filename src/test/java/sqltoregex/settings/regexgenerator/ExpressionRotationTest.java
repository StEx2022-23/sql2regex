package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.util.Arrays;
import java.util.List;

class ExpressionRotationTest {
    ExpressionRotation expressionRotation = new ExpressionRotation(SettingsOption.DEFAULT);
    List<Expression> testListOne = Arrays.asList(new Column("table1"), new Column("table2"));
    List<Expression> testListTwo = List.of(new Column("table1"));

    @Test
    void testExpressionRotationWithoutCapturingGroupTwoElements(){
        Assertions.assertEquals(
                "((\\s*(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*,\\s*(?:table2|able2|tble2|tale2|tabe2|tabl2|table)\\s*)|(\\s*(?:table2|able2|tble2|tale2|tabe2|tabl2|table)\\s*,\\s*(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*))",
                expressionRotation.generateRegExFor(testListOne));
    }

    @Test
    void testExpressionRotationWithoutCapturingGroupOneElement(){
        Assertions.assertEquals(
                "((\\s*(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*))",
                expressionRotation.generateRegExFor(testListTwo));
    }

    @Test
    void testExpressionRotationWithCapturingGroupOneElement(){
        expressionRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(?:(\\s*(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*))",
                expressionRotation.generateRegExFor(testListTwo));
    }
}
