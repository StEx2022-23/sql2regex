package sqltoregex.settings;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class UserSettingsTest {

    @Test
    void MapNotMutable() {
        Map<SettingsOption, RegExGenerator<?>> map = new HashMap<>();
        UserSettings userSettings1 = UserSettings.getInstance(map);
        Map<SettingsOption, RegExGenerator<?>> settingsMap = userSettings1.getSettingsMap();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> settingsMap.putAll(map));
        OrderRotation orderRotation = new OrderRotation(SettingsOption.DEFAULT);
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> settingsMap.put(SettingsOption.COLUMNNAMEORDER, orderRotation));
    }

    @Test
    void onlyOneInstance() {
        Map<SettingsOption, RegExGenerator<?>> map = new HashMap<>();
        UserSettings userSettings1 = UserSettings.getInstance(map);
        UserSettings userSettings2 = UserSettings.getInstance(map);
        Assertions.assertEquals(userSettings1, userSettings2);
    }

    @AfterAll
    static void tearDown() throws Exception {
        Field field = UserSettings.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(UserSettings.getInstance(), null);
    }
}
