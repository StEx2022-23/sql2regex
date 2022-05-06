package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultSynonymGeneratorTest {

    private DefaultSynonymGenerator defaultSynonymManager;

    @BeforeEach
    void beforeAll(){
        this.defaultSynonymManager = new DefaultSynonymGenerator();
        defaultSynonymManager.addSynonym("witzig");
        defaultSynonymManager.addSynonym("komisch");
        defaultSynonymManager.addSynonym("ulkig");
    }

    @Test
    public void queryNotExistingSynonym(){
        Assertions.assertEquals("traurig", defaultSynonymManager.generateSynonymRegexFor("traurig"));
    }

    @Test
    public void queryExistingSynonym(){
        Assertions.assertEquals("witzig|ulkig|komisch", defaultSynonymManager.generateSynonymRegexFor("witzig"));
        Assertions.assertEquals("ulkig|komisch|witzig", defaultSynonymManager.generateSynonymRegexFor("ulkig"));
    }
}
