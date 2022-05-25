package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

class InsertDeParserForRegExTest {
    @Test
    void testInserDeParserForRegExConstructor() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(new SettingsManager());
        Assertions.assertNotNull(insertDeParserForRegEx);
    }
}
