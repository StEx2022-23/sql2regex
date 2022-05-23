package sqltoregex.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

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
        Set<SettingsOption> settingsOptionSet = settingsManager.getSettingsMap().keySet();
        List<String> settingsOptionWhichHaveBeenSet = List.of(
                "KEYWORDSPELLING",
                "TABLENAMESPELLING",
                "TABLENAMEORDER",
                "COLUMNNAMESPELLING",
                "KEYWORDSPELLING",
                "COLUMNNAMEORDER",
                "DATESYNONYMS",
                "TIMESYNONYMS",
                "DATETIMESYNONYMS",
                "AGGREGATEFUNCTIONLANG",
                "NOT_AS_EXCLAMATION_AND_WORD"
        );
        for (String str : settingsOptionWhichHaveBeenSet) {
            Assertions.assertTrue(settingsOptionSet.toString().contains(str));
        }
    }

    @Test
    void testGetSettingByClazz(){
        Assertions.assertEquals(3, settingsManager.getSettingByClass(SpellingMistake.class).size());
        Assertions.assertEquals(2, settingsManager.getSettingByClass(OrderRotation.class).size());
        Assertions.assertEquals(1, settingsManager.getSettingByClass(StringSynonymGenerator.class).size());
    }

    @Test
    void testGetSetting(){
        for (SettingsOption settingsOption : SettingsOption.values()) {
            if(SettingsOption.DEFAULT.equals(settingsOption)){
                continue;
            }
            Assertions.assertTrue(settingsManager.getSettingsMap().containsKey(settingsOption), "Does not contain " + settingsOption);
            if (settingsOption == SettingsOption.NOT_AS_EXCLAMATION_AND_WORD) {
                Assertions.assertNull(settingsManager.getSettingsMap().get(settingsOption));
            } else {
                Assertions.assertNotNull(settingsManager.getSettingsMap().get(settingsOption));
            }
        }
    }
}
