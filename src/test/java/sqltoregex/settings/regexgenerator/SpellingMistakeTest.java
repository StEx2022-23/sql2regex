package sqltoregex.settings.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

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
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingMistake.generateRegExFor(input));
    }

    @Test
    void testSpellingMistakeOutputWithCapturingGroup() {
        String input = "test";
        spellingMistake.setNonCapturingGroup(true);
        String alternativeStyles = spellingMistake.generateRegExFor(input);
        Assertions.assertEquals("(?:test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testSpellingMistakeOutputWithNonCapturingGroup() {
        String input = "test";
        spellingMistake.setNonCapturingGroup(false);
        String alternativeStyles = spellingMistake.generateRegExFor(input);
        Assertions.assertEquals("(test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testGenerateAsList(){
        final String testString = "Spelling";
        Assertions.assertEquals(new LinkedList<>(List.of(testString)), SpellingMistake.generateAsListOrDefault(null, testString));

        List<String> spellings = List.of("Spelling",
                                         "pelling",
                                         "Selling",
                                         "Splling",
                                         "Speling",
                                         "Speling",
                                         "Spellng",
                                         "Spellig",
                                         "Spellin");

        Assertions.assertEquals(spellings, SpellingMistake.generateAsListOrDefault(new SpellingMistake(SettingsOption.DEFAULT), testString));
    }

    @Test
    void testUseOrDefault() {
        String keyword = "SELECT";
        List<String> keywordOptions = List.of("SELECT", "ELECT", "SLECT", "SELET", "SELEC");
        String generatedRegEx = SpellingMistake.useOrDefault(new SpellingMistake(SettingsOption.KEYWORDSPELLING),
                                                             keyword);
        for (String str : keywordOptions) {
            Assertions.assertTrue(generatedRegEx.contains(str));
        }

        String generatedRegExWithoutSpellingMistake = SpellingMistake.useOrDefault(null, keyword);
        Assertions.assertEquals("SELECT", generatedRegExWithoutSpellingMistake);
    }
}
