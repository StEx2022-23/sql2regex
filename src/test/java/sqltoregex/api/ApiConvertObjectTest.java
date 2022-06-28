package sqltoregex.api;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

class ApiConvertObjectTest {
    @Test
    void testConstructor() {
        Assertions.assertDoesNotThrow(ApiConvertObject::new);
        ApiConvertObject apiConvertObject = new ApiConvertObject();
        Assertions.assertNotNull(apiConvertObject);
    }

    @Test
    void testSqlSetterAndGetter() {
        ApiConvertObject apiConvertObject = new ApiConvertObject();
        LinkedList<String> linkedList = new LinkedList<>();
        Assertions.assertThrows(IllegalArgumentException.class, () -> apiConvertObject.setSql(linkedList));

        LinkedList<String> testList = new LinkedList<>();
        testList.add("test1");
        testList.add("test2");
        apiConvertObject.setSql(testList);
        Assertions.assertEquals(2, apiConvertObject.getSql().size());
    }

    @Test
    void testSettingsOptionSetterAndGetter() {
        ApiConvertObject apiConvertObject = new ApiConvertObject();
        apiConvertObject.setSettingsType(SettingsType.DEFAULT_SCHOOL);
        Assertions.assertEquals(SettingsType.DEFAULT_SCHOOL, apiConvertObject.getSettingsType());

        apiConvertObject.setSettingsType("DEFAULT_SCHOOL");
        Assertions.assertEquals(SettingsType.DEFAULT_SCHOOL, apiConvertObject.getSettingsType());
    }

    @Test
    void testRegExGen() throws XPathExpressionException, JSQLParserException, ParserConfigurationException, IOException, URISyntaxException, SAXException {
        ApiConvertObject apiConvertObject = new ApiConvertObject();
        apiConvertObject.setSettingsType(SettingsType.ALL);
        List<String> sqlList = new LinkedList<>();
        sqlList.add("SELECT *");
        sqlList.add("SELECT *");
        apiConvertObject.setSql(sqlList);

        List<String> regexList = apiConvertObject.getRegex();
        Assertions.assertEquals(2, regexList.size());

        for(String str : regexList){
            Assertions.assertEquals(
                    "^\\s*(?:SELECT|ELECT|SLECT|SEECT|SELCT|SELET|SELEC)\\s+(?:(?:ALL|LL|AL|AL)|\\*)\\s*;?\\s*$",
                    str
            );
        }
    }
}
