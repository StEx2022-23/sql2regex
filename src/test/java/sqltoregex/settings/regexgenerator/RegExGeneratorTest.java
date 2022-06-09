package sqltoregex.settings.regexgenerator;

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
import java.util.LinkedList;
import java.util.List;

class RegExGeneratorTest extends UserSettingsPreparer {

    public RegExGeneratorTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.USER);
    }

    @Test
    void testJoinListToRegEx(){
        List<String> stringList = new LinkedList<>(List.of("Peter", "Pahn"));
        String joinedString = RegExGenerator.joinListToRegEx(new OrderRotation(SettingsOption.DEFAULT), stringList);

        Assertions.assertEquals("(?:Peter|Pahn)", joinedString);
    }
}
