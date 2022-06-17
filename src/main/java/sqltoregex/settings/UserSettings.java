package sqltoregex.settings;

import org.springframework.web.context.annotation.RequestScope;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton UserSettings for store only one SettingsSet while generating regex.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
@RequestScope
public class UserSettings {
    private static UserSettings instance;
    private final SettingsContainer settingsContainer;

    /**
     * Constructor for user settings.
     * @param settingsContainer {@link SettingsContainer} with saved selected settings
     */
    private UserSettings(SettingsContainer settingsContainer) {
        this.settingsContainer = SettingsContainer.builder().with(settingsContainer).build();
    }

    /**
     * Returns the current user settings instance, or write new settings.
     * @param settingsContainer {@link SettingsContainer}
     * @return {@link UserSettings} instance
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
     * Returns the current user settings instance.
     * @return {@link UserSettings} instance
     */
    public static UserSettings getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User settings must be initialised before usage");
        }
        return instance;
    }

    /**
     * Checks if user settings instance is set.
     * @return boolean for is instance set?
     */
    public static boolean areSet() {
        return instance != null;
    }

    /**
     * Returns current SettingsContainer.
     * @return get current {@link SettingsContainer}
     */
    public SettingsContainer getSettingsContainer() {
        return this.settingsContainer;
    }
}
