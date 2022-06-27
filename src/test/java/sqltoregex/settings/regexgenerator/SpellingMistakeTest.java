package sqltoregex.settings.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.DEFAULT);

    private final String testString1 = "Spel";
    private final List<String> testSpellings1 = List.of(".?Spel",
                                           ".?pel",
                                           "S.?el",
                                           "Sp.?l",
                                           "Spe.?",
                                           "Spel.?"
    );

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
    void testSpellingMistakeOutputWithNonCapturingGroup() {
        spellingMistake.setNonCapturingGroup(true);
        String alternativeStyles = spellingMistake.generateRegExFor(testString1);
        Assertions.assertEquals("(?:" + String.join("|",testSpellings1) + ")", alternativeStyles);
    }

    @Test
    void testSpellingMistakeOutputWithCapturingGroup() {
        spellingMistake.setNonCapturingGroup(false);
        String alternativeStyles = spellingMistake.generateRegExFor(testString1);
        Assertions.assertEquals("(" + String.join("|",testSpellings1) + ")" , alternativeStyles);
    }

    @Test
    void testGenerateAsList(){
        Assertions.assertEquals(new LinkedList<>(List.of(testString1)), SpellingMistake.generateAsListOrDefault(null, testString1));

        Assertions.assertEquals(testSpellings1, SpellingMistake.generateAsListOrDefault(new SpellingMistake(SettingsOption.DEFAULT), testString1));
    }

    @Test
    void testUseOrDefault() {
        String generatedRegEx = SpellingMistake.useOrDefault(new SpellingMistake(SettingsOption.KEYWORDSPELLING),
                                                             testString1);
        for (String str : testSpellings1) {
            Assertions.assertTrue(generatedRegEx.contains(str));
        }

        String generatedRegExWithoutSpellingMistake = SpellingMistake.useOrDefault(null, testString1);
        Assertions.assertEquals("Spel", generatedRegExWithoutSpellingMistake);
    }
}
