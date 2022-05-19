package sqltoregex.settings;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UserSettings {
    private static UserSettings instance;
    public final ImmutableMap<SettingsOption, RegExGenerator<?>> SETTINGS_MAP;

    private UserSettings(Map<SettingsOption, RegExGenerator<?>> map) {
        this.SETTINGS_MAP = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?>>().putAll(map).build();
    }

    private UserSettings() {
        this.SETTINGS_MAP = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?>>().build();
    }

    public static UserSettings getInstance(Map<SettingsOption, RegExGenerator<?>> map) {
        if (instance == null) {
            instance = new UserSettings(map);
        }
        return instance;
    }

    public static UserSettings getInstance() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }

    public Map<SettingsOption, RegExGenerator<?>> getSettingsMap() {
        return this.SETTINGS_MAP;
    }
}
