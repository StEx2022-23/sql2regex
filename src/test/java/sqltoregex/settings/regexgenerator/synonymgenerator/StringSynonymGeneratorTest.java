package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

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
}
