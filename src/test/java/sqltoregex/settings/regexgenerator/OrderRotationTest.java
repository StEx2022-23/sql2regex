package sqltoregex.settings.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.deparser.TestUtils;
import sqltoregex.settings.SettingsOption;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

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
        orderRotation.setNonCapturingGroup(true);
        Assertions.assertEquals(
                "(?:table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithoutCapturingGroup() {
        orderRotation.setNonCapturingGroup(false);
        Assertions.assertEquals(
                "(table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithOneElement() {
        orderRotation.setNonCapturingGroup(false);
        Assertions.assertEquals(
                "(table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }

    @Test
    void testOrderRotationWithOneElementWithCapturingGroup() {
        orderRotation.setNonCapturingGroup(true);
        Assertions.assertEquals(
                "(?:table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }

    @Test
    void testGenerateAsList(){
        final List<String> testList = new LinkedList<>(List.of("1", "2", "3"));

        Assertions.assertEquals(testList, OrderRotation.generateAsListOrDefault(null, testList));

        List<String> rotations = List.of("1,2,3",
                                         "1,3,2",
                                         "2,1,3",
                                         "2,3,1",
                                         "3,1,2",
                                         "3,2,1");
        List<String> regexList =  OrderRotation.generateAsListOrDefault(new OrderRotation(SettingsOption.DEFAULT), testList);
        for (String rotation : rotations){
            String patternString = "";
            for (String s : regexList) {
                patternString = s;
                if (TestUtils.checkAgainstRegEx(patternString, rotation)) {
                    break;
                }
            }
            if (!TestUtils.checkAgainstRegEx(patternString, rotation)){
                fail(rotation + " Should have match at least 1 generated regex, but was: " + regexList);
            }

        }
    }

    @Test
    void testUseOrDefault() {
        List<String> stringList = new LinkedList<>();
        stringList.add("1");
        stringList.add("2");
        String orderRotatedExpections = "(?:1\\s*,\\s*2|2\\s*,\\s*1)";
        String orderRotatedList = OrderRotation.useOrDefault(new OrderRotation(SettingsOption.TABLENAMEORDER),
                                                             stringList);
        Assertions.assertEquals(orderRotatedExpections, orderRotatedList);

        List<String> nonRotatedList = new LinkedList<>();
        nonRotatedList.add("1");
        nonRotatedList.add("2");
        String nonOrderRotatedExpectation = "1\\s*,\\s*2";
        String regex = OrderRotation.useOrDefault(null, nonRotatedList);
        Assertions.assertEquals(nonOrderRotatedExpectation, regex);
    }
}
