package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.SpellingMistake;

class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake(PropertyOption.DEFAULT);

    @Test
    void testSpellingMistakeOutput(){
        String input = "test";
        String alternativeStyles = spellingMistake.calculateAlternativeWritingStyles(input);
        Assertions.assertEquals("(?:test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testSpellingMistakeOutputEmptyTablename(){
        String input = "";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String alternativeStyles = spellingMistake.calculateAlternativeWritingStyles(input);
        });
    }

    @Test
    void testGetProperty(){
        Assertions.assertEquals(1, spellingMistake.getSettings().size());
        Assertions.assertTrue(spellingMistake.getSettings().contains(PropertyOption.DEFAULT));
    }
}
