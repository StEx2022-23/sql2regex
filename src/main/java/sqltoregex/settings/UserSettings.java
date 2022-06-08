package sqltoregex.settings;

import org.springframework.web.context.annotation.RequestScope;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * singleton UserSettings for store only one SettingsSet while generating regex
 */
@RequestScope
public class UserSettings {
    private static UserSettings instance;
    private final SettingsContainer settingsContainer;

    /**
     * constructor for user settings
     * @param settingsContainer with saved selected settings
     */
    private UserSettings(SettingsContainer settingsContainer) {
        this.settingsContainer = SettingsContainer.builder().with(settingsContainer).build();
    }

    /**
     * return current user settings instance, or write new settings
     * @param settingsContainer
     * @return
     */
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

    /**
     * return current user settings instance
     * @return
     */
    public static UserSettings getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User settings must be initialised before usage");
        }
        return instance;
    }

    /**
     * check if user settings instance is set
     * @return
     */
    public static boolean areSet() {
        return instance != null;
    }

    /**
     * return current SettingsContainer
     * @return
     */
    public SettingsContainer getSettingsContainer() {
        return this.settingsContainer;
    }
}
