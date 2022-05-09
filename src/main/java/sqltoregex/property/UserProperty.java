package sqltoregex.property;

import java.util.Map;

class UserProperty{
    private static UserProperty instance;
    public Map<Class<Object>, Object> propertyMap;

    private UserProperty(Map<Class<Object>, Object> map){
        this.propertyMap = map;
    }

    public static UserProperty getInstance(Map<Class<Object>, Object> map) {
        if (instance == null){
            instance = new UserProperty(map);
        }
        return instance;
    }
}
