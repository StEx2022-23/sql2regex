package sqltoregex.PropertyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.OrderRotation;

import java.util.Arrays;
import java.util.List;

class OrderRotationTest {
    OrderRotation orderRotation = new OrderRotation();
    List<String> testListOne = Arrays.asList("table1", "table2");
    List<String> testListTwo = List.of("table1");

    @Test
    void testOrderRotationWithoutAlternativeWritingStyles(){
        Assertions.assertEquals(
                "(?:table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.calculateDifferentTableNameOrders(testListOne, false)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesOneElement(){
        Assertions.assertEquals(
                "(?:table1)",
                orderRotation.calculateDifferentTableNameOrders(testListTwo, false)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesNullArguments(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderRotation.calculateDifferentTableNameOrders(null, null));
    }

    @Test
    void testOrderRotationWithAlternativeWritingStyles(){
        Assertions.assertEquals(
                "(?:(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*,\\s*(?:table2|able2|tble2|tale2|tabe2|tabl2|table)|(?:table2|able2|tble2|tale2|tabe2|tabl2|table)\\s*,\\s*(?:table1|able1|tble1|tale1|tabe1|tabl1|table))",
                orderRotation.calculateDifferentTableNameOrders(testListOne, true)
        );
    }
}
