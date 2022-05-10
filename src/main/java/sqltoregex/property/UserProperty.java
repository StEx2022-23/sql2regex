package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public final ImmutableMap<PropertyOption, Property> propertyMap;

    private UserProperty(Map<PropertyOption, Property> map){
        this.propertyMap = new ImmutableMap.Builder<PropertyOption, Property>().putAll(map).build();
    }

    public static UserProperty getInstance(Map<PropertyOption, Property> map) {
        if (instance == null){
            instance = new UserProperty(map);
        }
        return instance;
    }
}
