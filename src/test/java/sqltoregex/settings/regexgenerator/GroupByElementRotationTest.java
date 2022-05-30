package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.deparser.UserSettingsPreparer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

class GroupByElementRotationTest extends UserSettingsPreparer {
    GroupByElementRotation groupByElementRotation = new GroupByElementRotation(SettingsOption.DEFAULT);
    List<Expression> testListOne = Arrays.asList(new Column("table1"), new Column("table2"));
    List<Expression> testListTwo = List.of(new Column("table1"));

    GroupByElementRotationTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException
            , URISyntaxException {
        super(SettingsType.USER);
    }

    @Test
    void testExpressionRotationWithCapturingGroupOneElement() {
        groupByElementRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(?:\\s*table1\\s*)",
                groupByElementRotation.generateRegExFor(testListTwo));
    }

    @Test
    void testExpressionRotationWithoutCapturingGroupOneElement() {
        Assertions.assertEquals(
                "(\\s*table1\\s*)",
                groupByElementRotation.generateRegExFor(testListTwo));
    }

    @Test
    void testExpressionRotationWithoutCapturingGroupTwoElements() {
        Assertions.assertEquals(
                "(\\s*table1\\s*,\\s*table2\\s*|\\s*table2\\s*,\\s*table1\\s*)",
                groupByElementRotation.generateRegExFor(testListOne));
    }
}
