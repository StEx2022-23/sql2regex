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

class UpdateDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    UpdateDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, URISyntaxException {
        super(SettingsType.ALL);
    }

    @Test
    void simpleUpdateStatement() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "UPDATE table1 SET col1 = 1 ORDER BY col1"
        );
        testUtils.validateListAgainstRegEx("UPDATE table1 SET col1 = 1 ORDER BY col1", toCheckedInput, true);
    }

    @Test
    void outputCLause() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2",
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col2, inserted.col1",
                "UPDATE table1 SET col1 = 1, col2 = 2  OUTUT  inserted.col2 , inserted.col1"
        );
        testUtils.validateListAgainstRegEx("UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2", toCheckedInput, true);
    }
}
