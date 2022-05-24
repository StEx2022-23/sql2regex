package sqltoregex.settings;

import org.junit.jupiter.api.AfterAll;
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
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
        Set<SettingsOption> settingsOptionSet = settingsManager.getDefaultSettingsMap().keySet();
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
                "EXPRESSIONORDER",
                "OTHERSYNONYMS",
                "NOT_AS_EXCLAMATION_AND_WORD"
        );
        for (String str : settingsOptionWhichHaveBeenSet) {
            Assertions.assertTrue(settingsOptionSet.toString().contains(str));
        }
    }

    @Test
    void usesUserSettings(){
        settingsManager.parseUserSettingsInput(new SettingsForm(new HashSet<>(List.of(SettingsOption.KEYWORDSPELLING)),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                ""));

        for (SettingsOption settingsOption :SettingsOption.values()){
            //catch default setted synonym manager
            if (settingsOption.equals(SettingsOption.KEYWORDSPELLING)
                    | settingsOption.equals(SettingsOption.DATESYNONYMS)
                    | settingsOption.equals(SettingsOption.TIMESYNONYMS)
                    | settingsOption.equals(SettingsOption.DATETIMESYNONYMS)
            ){
                Assertions.assertTrue(settingsManager.getSettingBySettingsOption(settingsOption), "Assertion failed for: " + settingsOption);
            }else{
                Assertions.assertFalse(settingsManager.getSettingBySettingsOption(settingsOption), "Assertion failed for: " + settingsOption);
            }
        }
    }

    @Test
    void testGetSettingByClazz(){
        Assertions.assertEquals(3, settingsManager.getSettingByClass(SpellingMistake.class, true).size());
        Assertions.assertEquals(2, settingsManager.getSettingByClass(OrderRotation.class, true).size());
        Assertions.assertEquals(1, settingsManager.getSettingByClass(StringSynonymGenerator.class, true).size());
    }

    @Test
    void testGetSetting(){
        for (SettingsOption settingsOption : SettingsOption.values()) {
            if(SettingsOption.DEFAULT.equals(settingsOption)){
                continue;
            }
            Assertions.assertTrue(settingsManager.getDefaultSettingsMap().containsKey(settingsOption), "Does not contain " + settingsOption);
            if (settingsOption == SettingsOption.NOT_AS_EXCLAMATION_AND_WORD) {
                Assertions.assertNull(settingsManager.getDefaultSettingsMap().get(settingsOption));
            } else {
                Assertions.assertNotNull(settingsManager.getDefaultSettingsMap().get(settingsOption));
            }
        }
    }
}
