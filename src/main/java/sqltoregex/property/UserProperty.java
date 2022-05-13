package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public final ImmutableMap<PropertyOption, Property<?>> propertyMap;

    private UserProperty(Map<PropertyOption, Property<?>> map){
        this.propertyMap = new ImmutableMap.Builder<PropertyOption, Property<?>>().putAll(map).build();
    }

    private UserProperty(){
        this.propertyMap = new ImmutableMap.Builder<PropertyOption, Property<?>>().build();
    }

    public static UserProperty getInstance(Map<PropertyOption, Property<?>> map) {
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

    public Map<PropertyOption, Property<?>> getPropertyMap(){
        return this.propertyMap;
    }
}
