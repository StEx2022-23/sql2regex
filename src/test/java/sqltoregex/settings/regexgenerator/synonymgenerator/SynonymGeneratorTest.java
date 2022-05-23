package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.junit.jupiter.api.*;
import sqltoregex.settings.SettingsOption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * weighted Graph testing ist not possible due to lack of equals and hashcode method overriding in Framework
 *
 * @Test void add2VerticesImplicitWeightedEdge(){
 * stringSynonymGenerator.addSynonym("Mittelwert");
 * stringSynonymGenerator.addSynonym("AVG", 2L);
 * Assertions.assertEquals(2, stringSynonymGenerator.synonymsGraph.edgeSet().toArray()[0]);
 * }
 * @Test void add2VerticesAndExplicitWeightedEdge(){
 * stringSynonymGenerator.addSynonymFor("Mittelwert", "AVG", 2L);
 * Assertions.assertEquals(2, stringSynonymGenerator.synonymsGraph.vertexSet().size());
 * Assertions.assertEquals(1, stringSynonymGenerator.synonymsGraph.edgeSet().size());
 * }
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class SynonymGeneratorTest {

    private StringSynonymGenerator stringSynonymGenerator;

    @Test
    void add2VerticesAndExplicitEdge() {
        stringSynonymGenerator.addSynonymFor("Mittelwert", "AVG");
        Assertions.assertEquals(2, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(1, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @Test
    void add2VerticesAndImplicitEdge() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        Assertions.assertEquals(2, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(1, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @Test
    void add6VerticesAndExplicitEdges() {
        stringSynonymGenerator.addSynonymFor("Mittelwert", "AVG");
        stringSynonymGenerator.addSynonymFor("Average", "AVG");
        stringSynonymGenerator.addSynonymFor("Durchschnitt", "AVG");

        stringSynonymGenerator.addSynonymFor("Summe", "SUM");
        Assertions.assertEquals(6, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(4, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @Test
    void add6VerticesAndExplicitEdgesReversed() {
        stringSynonymGenerator.addSynonymFor("AVG", "Mittelwert");
        stringSynonymGenerator.addSynonymFor("AVG", "Average");
        stringSynonymGenerator.addSynonymFor("AVG", "Durchschnitt");

        stringSynonymGenerator.addSynonymFor("Summe", "SUM");
        Assertions.assertEquals(6, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(4, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @Test
    void add3VerticesAndImplicitEdge() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonym("Average");
        Assertions.assertEquals(3, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(3, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @BeforeEach
    void beforeEach() {
        stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.DEFAULT);
    }

    @Test
    void equalsOverride() {
        StringSynonymGenerator stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();

        StringSynonymGenerator stringSynonymGenerator2 = getFullDefaultStringSynonymGenerator();

        Assertions.assertNotSame(stringSynonymGenerator1, stringSynonymGenerator2);
        Assertions.assertEquals(stringSynonymGenerator1, stringSynonymGenerator1);
        Assertions.assertEquals(stringSynonymGenerator1, stringSynonymGenerator2);

        stringSynonymGenerator1.removeSynonym("Average");
        Assertions.assertNotEquals(stringSynonymGenerator1, stringSynonymGenerator2);

        stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();
        stringSynonymGenerator1.setSuffix("");
        Assertions.assertNotEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());

        stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();
        stringSynonymGenerator1.setPrefix("");
        Assertions.assertNotEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());
    }

    private StringSynonymGenerator getFullDefaultStringSynonymGenerator() {
        StringSynonymGenerator _stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.DEFAULT);
        _stringSynonymGenerator.addSynonym("Mittelwert");
        _stringSynonymGenerator.addSynonym("AVG");
        _stringSynonymGenerator.addSynonym("Average");
        _stringSynonymGenerator.setPrefix("%");
        _stringSynonymGenerator.setSuffix("#");

        return _stringSynonymGenerator;
    }

    @Test
    void getSettings() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonym("Average");
        Set<String> stringSet = new HashSet<>(List.of("Mittelwert", "AVG", "Average"));
        Assertions.assertEquals(stringSet, stringSynonymGenerator.getGraph().vertexSet());
    }

    @Test
    void hashCodeOverride() {
        StringSynonymGenerator stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();
        StringSynonymGenerator stringSynonymGenerator2 = getFullDefaultStringSynonymGenerator();

        Assertions.assertNotEquals(System.identityHashCode(stringSynonymGenerator1),
                                   System.identityHashCode(stringSynonymGenerator2));
        Assertions.assertEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());

        stringSynonymGenerator1.removeSynonym("Mittelwert");
        Assertions.assertNotEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());

        stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();
        stringSynonymGenerator1.setSuffix("");
        Assertions.assertNotEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());

        stringSynonymGenerator1 = getFullDefaultStringSynonymGenerator();
        stringSynonymGenerator1.setPrefix("");
        Assertions.assertNotEquals(stringSynonymGenerator1.hashCode(), stringSynonymGenerator2.hashCode());
    }

    @Test
    void regExCreationCapturing() {
        stringSynonymGenerator.addSynonym("A");
        stringSynonymGenerator.setCapturingGroup(true);
        Assertions.assertEquals("(A)", stringSynonymGenerator.generateRegExFor("A"));
    }

    @Test
    void regExCreationNonCapturing() {
        stringSynonymGenerator.addSynonym("A");
        Assertions.assertEquals("(?:A)", stringSynonymGenerator.generateRegExFor("A"));
    }

    @Test
    void regExCreationWithPreSuffix() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonym("Average");
        stringSynonymGenerator.setPrefix("%");
        stringSynonymGenerator.setSuffix("#");
        Assertions.assertEquals("(?:%Mittelwert#|%Average#|%AVG#)",
                                stringSynonymGenerator.generateRegExFor("Mittelwert"));
    }

    @Test
    void regExCreationWithoutPreSuffix() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonym("Average");
        Assertions.assertEquals("(?:Mittelwert|Average|AVG)", stringSynonymGenerator.generateRegExFor("Mittelwert"));
    }

    @Test
    void removeVertex() {
        stringSynonymGenerator.addSynonym("Mittelwert");
        stringSynonymGenerator.addSynonym("AVG");
        stringSynonymGenerator.addSynonym("Average");
        stringSynonymGenerator.removeSynonym("AVG");
        Assertions.assertEquals(2, stringSynonymGenerator.synonymsGraph.vertexSet().size());
        Assertions.assertEquals(1, stringSynonymGenerator.synonymsGraph.edgeSet().size());
    }

    @Test
    void searchSynonymToStringDefaultImplementation() {
        Assertions.assertEquals("Mittelwert", stringSynonymGenerator.searchSynonymToString("Mittelwert"));
    }


}
