package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SettingsManagerTest {
    static SettingsManager settingsManager;
    static Set<SettingsOption> spellings;
    static Set<SettingsOption> orders;
    static Set<SimpleDateFormat> dateFormats;
    static Set<SimpleDateFormat> timeFormats;
    static Set<SimpleDateFormat> dateTimeFormats;
    static Set<String> aggregateFunctionLang;
    static String sql;

    @BeforeEach
    void beforeEach() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        this.resetSets();
    }

    void resetSets() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        settingsManager = new SettingsManager();
        spellings = new HashSet<>();
        orders = new HashSet<>();
        dateFormats = new HashSet<>();
        timeFormats = new HashSet<>();
        dateTimeFormats = new HashSet<>();
        aggregateFunctionLang = new HashSet<>();
        sql = "SELECT * FROM test";
    }

    @Test
    void testLoadDefaultProperties() {
        Set<SettingsOption> settingsOptionSet = settingsManager.readPropertyOptions();
        List<String> propertyOptionWhichHaveBeenSet = List.of(
                "KEYWORDSPELLING",
                "TABLENAMESPELLING",
                "TABLENAMEORDER",
                "COLUMNNAMESPELLING",
                "KEYWORDSPELLING",
                "COLUMNNAMEORDER",
                "DATESYNONYMS",
                "TIMESYNONYMS",
                "DATETIMESYNONYMS",
                "AGGREGATEFUNCTIONLANG"
        );
        for (String str : propertyOptionWhichHaveBeenSet) {
            Assertions.assertTrue(settingsOptionSet.toString().contains(str));
        }
    }

    @Test
    void testGetPropertyByClazz(){
        Assertions.assertEquals(3, settingsManager.getPropertyByClass(SpellingMistake.class).size());
        Assertions.assertEquals(2, settingsManager.getPropertyByClass(OrderRotation.class).size());
        Assertions.assertEquals(1, settingsManager.getPropertyByClass(StringSynonymGenerator.class).size());
    }

    @Test
    void testGetProperty(){
        for (SettingsOption settingsOption : SettingsOption.values()) {
            if(SettingsOption.DEFAULT.equals(settingsOption)){
                continue;
            }
            Assertions.assertTrue(settingsManager.getPropertyMap().containsKey(settingsOption));
            Assertions.assertNotNull(settingsManager.getPropertyMap().get(settingsOption));
        }
    }
}
