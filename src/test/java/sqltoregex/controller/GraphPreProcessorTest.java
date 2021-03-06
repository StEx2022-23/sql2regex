package sqltoregex.controller;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

class GraphPreProcessorTest {

    private final SettingsManager settingsManager = new SettingsManager();

    GraphPreProcessorTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException,
            URISyntaxException {
    }

    @Test
    void getSynonymMap() {
        Map<String, Set<String>> map = GraphPreProcessor.getSynonymMap(settingsManager.getSettingBySettingsOption(
                        SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                                                                               .map(SynonymGenerator::getGraph)
                                                                               .orElse(new SimpleGraph<>(
                                                                                       DefaultWeightedEdge.class)));
        Assertions.assertTrue(map.containsKey("AVG"));
        Assertions.assertTrue(map.containsKey("SUM"));
        Assertions.assertTrue(map.containsKey("COUNT"));
        Assertions.assertTrue(map.containsKey("MIN"));
        Assertions.assertTrue(map.containsKey("MAX"));
        Assertions.assertEquals(5, map.size());
    }
}
