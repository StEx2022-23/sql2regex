package sqltoregex.settings;

import com.google.common.collect.ImmutableMap;
import sqltoregex.settings.regexgenerator.RegExGenerator;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSettings {
    private static UserSettings instance;
    private final ImmutableMap<SettingsOption, RegExGenerator<?>> settingsMap;

    private UserSettings(Map<SettingsOption, RegExGenerator<?>> map) {
        this.settingsMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?>>().putAll(map).build();
    }

    public static UserSettings getInstance(Map<SettingsOption, RegExGenerator<?>> map) {
        if (instance == null) {
            instance = new UserSettings(map);
        }else{
            if (!instance.getSettingsMap().equals(map)){
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Wanted to create another instance with other settings");
            }
        }
        return instance;
    }

    public static UserSettings getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User settings must be initialised before usage");
        }
        return instance;
    }

    public static boolean areSet(){
        return instance != null;
    }

    public Map<SettingsOption, RegExGenerator<?>> getSettingsMap() {
        return this.settingsMap;
    }
}
