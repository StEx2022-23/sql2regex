package sqltoregex.settings;

import org.springframework.web.context.annotation.RequestScope;
import sqltoregex.settings.regexgenerator.IRegExGenerator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScope
public class UserSettings {
    private static UserSettings instance;
    private final Map<SettingsOption, IRegExGenerator<?>> settingsMap;

    private UserSettings(Map<SettingsOption, IRegExGenerator<?>> map) {
        this.settingsMap = Collections.unmodifiableMap(new EnumMap<>(map));
    }

    public static UserSettings getInstance(Map<SettingsOption, IRegExGenerator<?>> map) {
        if (instance == null) {
            instance = new UserSettings(map);
        } else {
            if (!instance.getSettingsMap().equals(map)) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
                        .log(Level.INFO, "Wanted to create another instance with other settings");
                instance = new UserSettings(map);
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

    public static boolean areSet() {
        return instance != null;
    }

    public Map<SettingsOption, IRegExGenerator<?>> getSettingsMap() {
        return this.settingsMap;
    }
}
