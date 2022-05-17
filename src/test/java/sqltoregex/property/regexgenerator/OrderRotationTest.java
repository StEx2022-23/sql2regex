package sqltoregex.property.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.SettingsOption;

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
        orderRotation2.setSpellingMistake(new SpellingMistake(SettingsOption.DEFAULT));
        Assertions.assertNotEquals(orderRotation1, orderRotation2);
    }

    @Test
    void getSetting() {
        OrderRotation orderRotation = new OrderRotation(SettingsOption.COLUMNNAMEORDER);
        Assertions.assertEquals(1, orderRotation.getSettings().size());
        Assertions.assertTrue(orderRotation.getSettings().contains(SettingsOption.COLUMNNAMEORDER));
    }

    @Test
    void testOrderRotationWithAlternativeWritingStyles() {
        SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.DEFAULT);
        spellingMistake.setCapturingGroup(true);
        orderRotation.setSpellingMistake(spellingMistake);
        orderRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "((table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*,\\s*(table2|able2|tble2|tale2|tabe2|tabl2|table)|" +
                        "(table2|able2|tble2|tale2|tabe2|tabl2|table)\\s*,\\s*" +
                        "(table1|able1|tble1|tale1|tabe1|tabl1|table))",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithAlternativeWritingStylesWithCapturingGroup() {
        SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.DEFAULT);
        spellingMistake.setCapturingGroup(false);
        orderRotation.setSpellingMistake(spellingMistake);
        orderRotation.setCapturingGroup(false);
        Assertions.assertEquals(
                "(?:(?:table1|able1|tble1|tale1|tabe1|tabl1|table)\\s*,\\s*" +
                        "(?:table2|able2|tble2|tale2|tabe2|tabl2|table)|" +
                        "(?:table2|able2|tble2|tale2|tabe2|tabl2|table)\\s*,\\s*" +
                        "(?:table1|able1|tble1|tale1|tabe1|tabl1|table))",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStyles() {
        orderRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesNullArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderRotation.generateRegExFor(null));
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesOneElement() {
        orderRotation.setCapturingGroup(true);
        Assertions.assertEquals(
                "(table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesOneElementWithCapturingGroup() {
        orderRotation.setCapturingGroup(false);
        Assertions.assertEquals(
                "(?:table1)",
                orderRotation.generateRegExFor(testListTwo)
        );
    }

    @Test
    void testOrderRotationWithoutAlternativeWritingStylesWithCapturingGroup() {
        orderRotation.setCapturingGroup(false);
        Assertions.assertEquals(
                "(?:table1\\s*,\\s*table2|table2\\s*,\\s*table1)",
                orderRotation.generateRegExFor(testListOne)
        );
    }
}
