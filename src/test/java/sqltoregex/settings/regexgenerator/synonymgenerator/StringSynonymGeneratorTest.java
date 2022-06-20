package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

class StringSynonymGeneratorTest {

    private StringSynonymGenerator defaultSynonymManager;

    @BeforeEach
    void beforeAll() {
        this.defaultSynonymManager = new StringSynonymGenerator(SettingsOption.DEFAULT);
        defaultSynonymManager.addSynonym("witzig");
        defaultSynonymManager.addSynonym("komisch");
        defaultSynonymManager.addSynonym("ulkig");
        defaultSynonymManager.setNonCapturingGroup(false);
    }

    @Test
    void queryExistingSynonym() {
        Assertions.assertEquals("(witzig|ulkig|komisch)", defaultSynonymManager.generateRegExFor("witzig"));
        Assertions.assertEquals("(ulkig|komisch|witzig)", defaultSynonymManager.generateRegExFor("ulkig"));
        Assertions.assertEquals("(komisch|ulkig|witzig)", defaultSynonymManager.generateRegExFor("komisch"));
    }

    @Test
    void queryNotExistingSynonym() {
        Assertions.assertEquals("(traurig)", defaultSynonymManager.generateRegExFor("traurig"));
    }

    @Test
    void testGenerateAsList(){
        final String testString = "syn1";
        Assertions.assertEquals(new LinkedList<>(List.of(testString)), StringSynonymGenerator.generateAsListOrDefault(null, testString));

        StringSynonymGenerator generator = new StringSynonymGenerator(SettingsOption.DEFAULT);
        generator.addSynonym("syn1");
        generator.addSynonym("syn2");
        generator.addSynonym("syn3");

        List<String> synonyms = List.of("syn1",
                                        "syn2",
                                        "syn3");

        List<String> generatedSynonyms = StringSynonymGenerator.generateAsListOrDefault(generator, testString);
        Assertions.assertEquals(synonyms.size(), generatedSynonyms.size());
        for (String synonym : generatedSynonyms){
            Assertions.assertTrue(synonyms.contains(synonym));
        }
    }

    @Test
    void testUseOrDefault() {
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.DEFAULT);
        stringSynonymGenerator.addSynonym("Cola");
        stringSynonymGenerator.addSynonym("Coke");
        String regex = StringSynonymGenerator.useOrDefault(stringSynonymGenerator, "Cola");

        Assertions.assertTrue(regex.contains("Cola"));
        Assertions.assertTrue(regex.contains("Coke"));

        Assertions.assertEquals("Cola", StringSynonymGenerator.useOrDefault(null, "Cola"));
    }
}
