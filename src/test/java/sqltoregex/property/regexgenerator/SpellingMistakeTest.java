package sqltoregex.property.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.SettingsOption;

class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.DEFAULT);

    @Test
    void equals() {
        Assertions.assertEquals(new SpellingMistake(SettingsOption.DEFAULT),
                                new SpellingMistake(SettingsOption.DEFAULT));
    }

    @Test
    void testGetProperty() {
        Assertions.assertEquals(1, spellingMistake.getSettings().size());
        Assertions.assertTrue(spellingMistake.getSettings().contains(SettingsOption.DEFAULT));
    }

    @Test
    void testSpellingMistakeOutputEmptyTableName() {
        String input = "";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String alternativeStyles = spellingMistake.generateRegExFor(input);
        });
    }

    @Test
    void testSpellingMistakeOutputWitCapturingGroup() {
        String input = "test";
        spellingMistake.setCapturingGroup(true);
        String alternativeStyles = spellingMistake.generateRegExFor(input);
        Assertions.assertEquals("(test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testSpellingMistakeOutputWithNonCapturingGroup() {
        String input = "test";
        String alternativeStyles = spellingMistake.generateRegExFor(input);
        Assertions.assertEquals("(?:test|est|tst|tet|tes)", alternativeStyles);
    }
}
