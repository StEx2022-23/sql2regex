package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public final ImmutableMap<PropertyOption, RegExGenerator<?, ?>> propertyMap;

    private UserProperty(Map<PropertyOption, RegExGenerator<?, ?>> map){
        this.propertyMap = new ImmutableMap.Builder<PropertyOption, RegExGenerator<?, ?>>().putAll(map).build();
    }

    private UserProperty(){
        this.propertyMap = new ImmutableMap.Builder<PropertyOption, RegExGenerator<?, ?>>().build();
    }

    public static UserProperty getInstance(Map<PropertyOption, RegExGenerator<?, ?>> map) {
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

    public Map<PropertyOption, RegExGenerator<?, ?>> getPropertyMap(){
        return this.propertyMap;
    }
}
