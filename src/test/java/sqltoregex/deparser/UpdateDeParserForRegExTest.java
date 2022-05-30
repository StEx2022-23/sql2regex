package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class UpdateDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    public UpdateDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, URISyntaxException {
        super(SettingsType.ALL);
    }

    @Test
    void simpleUpdateStatement() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "UPDATE table1 SET col1 = 1 ORDER BY col1"
        );
        testUtils.validateListAgainstRegEx("UPDATE table1 SET col1 = 1 ORDER BY col1", toCheckedInput, true);
    }
}
