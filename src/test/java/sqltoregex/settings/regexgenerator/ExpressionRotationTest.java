package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.deparser.UserSettingsTestCase;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

class ExpressionRotationTest extends UserSettingsTestCase {
    ExpressionRotation expressionRotation = new ExpressionRotation(SettingsOption.DEFAULT);
    List<Expression> testListOne = Arrays.asList(new Column("table1"), new Column("table2"));
    List<Expression> testListTwo = List.of(new Column("table1"));

    ExpressionRotationTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, URISyntaxException {
        super(SettingsType.USER);
    }

    @Test
    void testExpressionRotationWithoutCapturingGroupTwoElements(){
        Assertions.assertEquals(
                "((\\s*table1\\s*,\\s*table2\\s*)|(\\s*table2\\s*,\\s*table1\\s*))",
                expressionRotation.generateRegExFor(testListOne));
    }

    @Test
    void testExpressionRotationWithoutCapturingGroupOneElement(){
        Assertions.assertEquals(
                "((\\s*table1\\s*))",
                expressionRotation.generateRegExFor(testListTwo));
    }

    @Test
    void testExpressionRotationWithCapturingGroupOneElement(){
        expressionRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(?:(\\s*table1\\s*))",
                expressionRotation.generateRegExFor(testListTwo));
    }
}
