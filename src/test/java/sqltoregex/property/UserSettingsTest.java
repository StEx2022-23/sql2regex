package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.regexgenerator.OrderRotation;

import java.util.HashMap;
import java.util.Map;

class UserSettingsTest {

    @Test
    void MapNotMutable() {
        Map<SettingsOption, RegExGenerator<?, ?>> map = new HashMap<>();
        UserSettings userSettings1 = UserSettings.getInstance(map);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userSettings1.settingsMap.putAll(map));
        OrderRotation orderRotation = new OrderRotation(SettingsOption.DEFAULT);
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> userSettings1.settingsMap.put(SettingsOption.COLUMNNAMEORDER, orderRotation));
    }

    @Test
    void onlyOneInstance() {
        Map<SettingsOption, RegExGenerator<?, ?>> map = new HashMap<>();
        UserSettings userSettings1 = UserSettings.getInstance(map);
        UserSettings userSettings2 = UserSettings.getInstance(map);
        Assertions.assertEquals(userSettings1, userSettings2);
    }
}
