package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSettings {
    private static UserSettings instance;
    public final ImmutableMap<SettingsOption, RegExGenerator<?, ?>> settingsMap;

    private UserSettings(Map<SettingsOption, RegExGenerator<?, ?>> map){
        this.settingsMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().putAll(map).build();
    }

    private UserSettings(){
        this.settingsMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().build();
    }

    public static UserSettings getInstance(Map<SettingsOption, RegExGenerator<?, ?>> map) {
        if (instance == null){
            instance = new UserSettings(map);
        }
        return instance;
    }

    public static UserSettings getInstance() {
        if (instance == null){
            instance = new UserSettings();
        }
        return instance;
    }

    public Map<SettingsOption, RegExGenerator<?, ?>> getSettingsMap(){
        return this.settingsMap;
    }
}
