package sqltoregex.settings;

import org.springframework.web.context.annotation.RequestScope;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScope
public class UserSettings {
    private static UserSettings instance;
    private final SettingsContainer settingsContainer;

    private UserSettings(SettingsContainer settingsContainer) {
        this.settingsContainer = SettingsContainer.builder().with(settingsContainer).build();
    }

    public static UserSettings getInstance(SettingsContainer settingsContainer) {
        if (instance == null) {
            instance = new UserSettings(settingsContainer);
        } else {
            if (!instance.getSettingsContainer().equals(settingsContainer)) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
                        .log(Level.INFO, "Wanted to create another instance with other settings");
                instance = new UserSettings(settingsContainer);
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

    public SettingsContainer getSettingsContainer() {
        return this.settingsContainer;
    }
}
