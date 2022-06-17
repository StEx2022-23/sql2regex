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
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

class SettingsManagerTest {
    static SettingsManager settingsManager;
    static Set<SettingsOption> spellings;
    static Set<SettingsOption> orders;
    static Set<SimpleDateFormat> dateFormats;
    static Set<SimpleDateFormat> timeFormats;
    static Set<SimpleDateFormat> dateTimeFormats;
    static Set<String> aggregateFunctionLang;
    static String sql;

    @AfterAll
    static void tearDown() throws Exception {
        Field field = UserSettings.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(UserSettings.getInstance(), null);
    }

    @BeforeEach
    void beforeEach() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException,
            URISyntaxException {
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
    void testGetSetting() {
        for (SettingsOption settingsOption : SettingsOption.values()) {
            if (SettingsOption.DEFAULT.equals(settingsOption)) {
                continue;
            }
            Assertions.assertTrue(settingsManager.getSettingsContainer(SettingsType.ALL).getAllSettings().containsKey(settingsOption),
                                  "Does not contain " + settingsOption);
            Assertions.assertNotNull(settingsManager.getSettingsContainer(SettingsType.ALL).get(settingsOption));
        }
    }

    @Test
    void testGetSettingByClazz() {
        Assertions.assertEquals(4, settingsManager.getSettingByClass(SpellingMistake.class, SettingsType.ALL).size());
        Assertions.assertEquals(5, settingsManager.getSettingByClass(OrderRotation.class, SettingsType.ALL).size());
        Assertions.assertEquals(3, settingsManager.getSettingByClass(StringSynonymGenerator.class, SettingsType.ALL)
                .size());
    }

    @Test
    void testLoadAllProperties() {
        Set<SettingsOption> settingsOptionSet = settingsManager.getSettingsContainer(SettingsType.ALL).getAllSettings().keySet();
        for (SettingsOption option
                : Arrays.stream(SettingsOption.values())
                .filter(settingsOption -> settingsOption != SettingsOption.DEFAULT).toList()) {
            Assertions.assertTrue(settingsOptionSet.contains(option), "All does not contain:" + option);
        }
    }

    @Test
    void usesUserSettings() {
        settingsManager.parseUserSettingsInput(new SettingsForm(new HashSet<>(List.of(SettingsOption.KEYWORDSPELLING)),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                Collections.emptySet(),
                                                                "")
        );

        for (SettingsOption settingsOption : SettingsOption.values()) {
            //catch default setted synonym manager
            if (settingsOption.equals(SettingsOption.KEYWORDSPELLING)
                    | settingsOption.equals(SettingsOption.DATESYNONYMS)
                    | settingsOption.equals(SettingsOption.TIMESYNONYMS)
                    | settingsOption.equals(SettingsOption.DATETIMESYNONYMS)
            ) {
                Assertions.assertTrue(settingsManager.getSettingBySettingsOption(settingsOption),
                                      "Assertion failed for: " + settingsOption);
            } else {
                Assertions.assertFalse(settingsManager.getSettingBySettingsOption(settingsOption),
                                       "Assertion failed for: " + settingsOption);
            }
        }
    }
}
