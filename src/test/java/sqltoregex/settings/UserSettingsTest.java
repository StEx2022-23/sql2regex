package sqltoregex.settings;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

class UserSettingsTest {

    @AfterAll
    static void tearDown() throws Exception {
        Field field = UserSettings.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(UserSettings.getInstance(), null);
    }

    @Test
    void MapNotMutable() {
        SettingsContainer.Builder builder = SettingsContainer.builder();
        builder.with(SettingsContainer.builder().build());
        UserSettings userSettings1 = UserSettings.getInstance();
        Map<SettingsOption, IRegExGenerator<?>> settingsMap = userSettings1.getSettingsContainer().getAllSettings();
        Map<SettingsOption, IRegExGenerator<?>> map = Collections.emptyMap();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> settingsMap.putAll(map));
        OrderRotation orderRotation = new OrderRotation(SettingsOption.DEFAULT);
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> settingsMap.put(SettingsOption.COLUMNNAMEORDER, orderRotation));
    }

    @Test
    void onlyOneInstance() {
        SettingsContainer.Builder settingsContainerBuilder = SettingsContainer.builder();
        SettingsContainer settingsContainer = settingsContainerBuilder.build();

        UserSettings userSettings1 = UserSettings.getInstance(settingsContainer);
        UserSettings userSettings2 = UserSettings.getInstance(settingsContainer);
        Assertions.assertEquals(userSettings1, userSettings2);
    }
}
