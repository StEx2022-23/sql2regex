package sqltoregex.property;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public ImmutableMap<Class<Property>, Property> propertyMap;

    private UserProperty(Map<Class<Property>, Property> map){
        this.propertyMap = new ImmutableMap.Builder<Class<Property>, Property>().putAll(map).build();
    }

    public static UserProperty getInstance(Map<Class<Property>, Property> map) {
        if (instance == null){
            instance = new UserProperty(map);
        }
        return instance;
    }
}
