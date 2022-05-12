package sqltoregex.property.regexgenerator.synonymgenerator;

import org.junit.jupiter.api.BeforeEach;
import sqltoregex.property.PropertyOption;

public class SynonymGeneratorTest {

    private StringSynonymGenerator stringSynonymGenerator;

    @BeforeEach
    void beforeEach(){
        stringSynonymGenerator = new StringSynonymGenerator(PropertyOption.DEFAULT);
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonymFor("Mittelwert", "AVG");
    }
}
