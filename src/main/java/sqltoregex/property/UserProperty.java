package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public final ImmutableMap<SettingsOption, RegExGenerator<?, ?>> propertyMap;

    private UserProperty(Map<SettingsOption, RegExGenerator<?, ?>> map){
        this.propertyMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().putAll(map).build();
    }

    private UserProperty(){
        this.propertyMap = new ImmutableMap.Builder<SettingsOption, RegExGenerator<?, ?>>().build();
    }

    public static UserProperty getInstance(Map<SettingsOption, RegExGenerator<?, ?>> map) {
        if (instance == null){
            instance = new UserProperty(map);
        }
        return instance;
    }

    public static UserProperty getInstance() {
        if (instance == null){
            instance = new UserProperty();
        }
        return instance;
    }

    public Map<SettingsOption, RegExGenerator<?, ?>> getPropertyMap(){
        return this.propertyMap;
    }
}
