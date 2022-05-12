package sqltoregex.property.regexgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.PropertyOption;
import sqltoregex.property.regexgenerator.SpellingMistake;

class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake(PropertyOption.DEFAULT);

    @Test
    void testSpellingMistakeOutput(){
        String input = "test";
        String alternativeStyles = spellingMistake.generateRegExFor(input);
        Assertions.assertEquals("(?:test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testSpellingMistakeOutputEmptyTablename(){
        String input = "";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String alternativeStyles = spellingMistake.generateRegExFor(input);
        });
    }

    @Test
    void testGetProperty(){
        Assertions.assertEquals(1, spellingMistake.getSettings().size());
        Assertions.assertTrue(spellingMistake.getSettings().contains(PropertyOption.DEFAULT));
    }

    @Test
    void equals(){
        Assertions.assertEquals(new SpellingMistake(PropertyOption.DEFAULT), new SpellingMistake(PropertyOption.DEFAULT));
    }
}
