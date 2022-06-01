package sqltoregex.settings.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.util.Arrays;
import java.util.List;

class OrderRotationTest {
    OrderRotation orderRotation = new OrderRotation(SettingsOption.DEFAULT);
    List<String> testListOne = Arrays.asList("table1", "table2");
    List<String> testListTwo = List.of("table1");

    @Test
    void equals() {
        OrderRotation orderRotation1 = new OrderRotation(SettingsOption.DEFAULT);
        OrderRotation orderRotation2 = new OrderRotation(SettingsOption.DEFAULT);
        Assertions.assertEquals(orderRotation1, orderRotation2);
        Assertions.assertNotEquals(orderRotation1, new OrderRotation(SettingsOption.COLUMNNAMEORDER));
    }

    @Test
    void testOrderRotationWithCapturingGroup() {
        orderRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(?:table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithoutCapturingGroup() {
        orderRotation.setCapturingGroup(false);
        Assertions.assertEquals(
                "(table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithOneElement() {
        orderRotation.setCapturingGroup(false);
        Assertions.assertEquals(
                "(table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }

    @Test
    void testOrderRotationWithOneElementWithCapturingGroup() {
        orderRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(?:table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }
}
