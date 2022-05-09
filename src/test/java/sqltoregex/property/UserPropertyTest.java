package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserPropertyTest {

    @Test
    public void onlyOneInstance(){
        Map<Class<Property>, Property> map = new HashMap<>();
        UserProperty userProperty1 =  UserProperty.getInstance(map);
        UserProperty userProperty2 = UserProperty.getInstance(map);
        Assertions.assertEquals(userProperty1, userProperty2);
    }

    @Test
    public void MapNotMutable(){
        Map<Class<Property>, Property> map = new HashMap<>();
        UserProperty userProperty1 =  UserProperty.getInstance(map);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userProperty1.propertyMap.putAll(map));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userProperty1.propertyMap.put(OrderRotation.class);
    }
}
