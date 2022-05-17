package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UserSettings {
    private static UserSettings instance;
    public final ImmutableMap<SettingsOption, RegExGenerator<?, ?>> propertyMap;

    private UserSettings(Map<SettingsOption, RegExGenerator<?, ?>> map){
        this.propertyMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().putAll(map).build();
    }

    private UserSettings(){
        this.propertyMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().build();
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

    public Map<SettingsOption, RegExGenerator<?, ?>> getPropertyMap(){
        return this.propertyMap;
    }
}
