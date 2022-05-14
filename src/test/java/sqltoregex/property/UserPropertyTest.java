package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.regexgenerator.OrderRotation;

import java.util.HashMap;
import java.util.Map;

class UserPropertyTest {

    @Test
    void MapNotMutable() {
        Map<PropertyOption, Property<?>> map = new HashMap<>();
        UserProperty userProperty1 = UserProperty.getInstance(map);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userProperty1.propertyMap.putAll(map));
        OrderRotation orderRotation = new OrderRotation(PropertyOption.DEFAULT);
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> userProperty1.propertyMap.put(PropertyOption.COLUMNNAMEORDER, orderRotation));
    }

    @Test
    void onlyOneInstance() {
        Map<PropertyOption, Property<?>> map = new HashMap<>();
        UserProperty userProperty1 = UserProperty.getInstance(map);
        UserProperty userProperty2 = UserProperty.getInstance(map);
        Assertions.assertEquals(userProperty1, userProperty2);
    }
}
