package sqltoregex.PropertyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sqltoregex.property.SpellingMistake;

@SpringBootTest
class SpellingMistakeTest {
    public SpellingMistake spellingMistake = new SpellingMistake();

    @Test
    void testSpellingMistakeOutput(){
        String input = "test";
        String alternativeStyles = spellingMistake.calculateAlternativeWritingStyles(input);
        Assertions.assertEquals("(?:test|est|tst|tet|tes)", alternativeStyles);
    }

    @Test
    void testGetProperty(){
        Assertions.assertEquals(1, spellingMistake.getSettings().size());
        Assertions.assertEquals("SpellingMistake", spellingMistake.getSettings().get(0));
    }
}
