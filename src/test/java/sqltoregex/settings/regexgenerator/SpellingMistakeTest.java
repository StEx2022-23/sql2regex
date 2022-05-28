package sqltoregex.settings.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.DEFAULT);

    @Test
    void equals() {
        Assertions.assertEquals(new SpellingMistake(SettingsOption.DEFAULT),
                                new SpellingMistake(SettingsOption.DEFAULT));
    }

    @Test
    void testSpellingMistakeOutputEmptyTableName() {
        String input = "";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String alternativeStyles = spellingMistake.generateRegExFor(input);
        });
    }

    @Test
    void testSpellingMistakeOutputWithCapturingGroup() {
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
