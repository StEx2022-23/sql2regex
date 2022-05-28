package sqltoregex.deparser;

import org.junit.jupiter.api.AfterAll;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsForm;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;
import sqltoregex.settings.UserSettings;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

public class UserSettingsPreparer {

    protected final SettingsManager settingsManager = new SettingsManager();

    public UserSettingsPreparer(
            SettingsType settingsType) throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        if (settingsType == SettingsType.USER) {
            this.settingsManager.parseUserSettingsInput(SettingsForm.EMPTY_FORM);
        } else {
            UserSettings.getInstance(settingsManager.getSettingsMap(settingsType));
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        Field field = UserSettings.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(UserSettings.getInstance(), null);
    }
}
