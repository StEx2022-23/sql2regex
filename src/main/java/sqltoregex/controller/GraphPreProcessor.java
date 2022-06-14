package sqltoregex.controller;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * They prepare graphs for displaying on the frontend.
 */
public class GraphPreProcessor {
    public static final String COULD_NOT_CAST = "Could not cast";

    /**
     * The private constructor to hide the implicit public one.
     * Utility classes, which are collections of static members, are not meant to be instantiated.
     */
    private GraphPreProcessor() {
        throw new IllegalStateException("Utility class.");
    }

    /**
     * Return a set of synonyms from the given graph.
     * @param graph to process graph
     * @return set of synonyms
     */
    static <T> Set<T> getSynonymSet(Graph<T, DefaultWeightedEdge> graph) {
        return new LinkedHashSet<>(graph.vertexSet());
    }

    /**
     * Return a set of synonyms from the given graph, which are delimited.
     * @param graph to process graph
     * @return set of synonyms as delimited entrys
     */
    static Set<String> getSynonymSetWithDelimiter(Graph<String, DefaultWeightedEdge> graph, String delimiter) {
        Set<String> synonymSet = new LinkedHashSet<>();
        try {
            Map<String, Set<String>> map = getSynonymMap(graph);
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                for (String setValue : entry.getValue()) {
                    synonymSet.add(entry.getKey() + delimiter + setValue);
                }
            }
            return synonymSet;
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, COULD_NOT_CAST, e);
            return Collections.emptySet();
        }
    }

    /**
     * Return a set of synonyms from the given graph, only as strings.
     * @param graph to process graph
     * @return set of synonyms
     */
    static Set<String> getSynonymSetAsString(Graph<?, DefaultWeightedEdge> graph) {
        try {
            Set<String> stringSet = new LinkedHashSet<>();
            Optional<?> optional = graph.vertexSet().stream().findFirst();
            if (optional.isPresent()) {
                if (optional.get() instanceof SimpleDateFormat) {
                    for (Object format : graph.vertexSet()) {
                        stringSet.add(((SimpleDateFormat) format).toPattern());
                    }
                } else {
                    for (Object value : graph.vertexSet()) {
                        stringSet.add(value.toString());
                    }
                }
            }
            return stringSet;
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, COULD_NOT_CAST, e);
            return Collections.emptySet();
        }
    }

    /**
     * Generate a map of keywords and their related synonyms.
     * @param graph to process graph
     * @return map with keywords as key and their related synonyms as set
     */
    static <T> Map<T, Set<T>> getSynonymMap(Graph<T, DefaultWeightedEdge> graph) {
        try {
            Map<T, Set<T>> map = new HashMap<>();
            Set<DefaultWeightedEdge> edgeSet = new HashSet<>(graph.edgeSet());
            for (DefaultWeightedEdge edge : edgeSet) {
                T source = graph.getEdgeSource(edge);
                T target = graph.getEdgeTarget(edge);

                Set<T> synonyms = map.get(source);
                if (synonyms == null){
                    map.put(source, new LinkedHashSet<>(List.of(target)));
                }else{
                    synonyms.add(target);
                }
            }
            return map;
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, COULD_NOT_CAST, e);
            return Collections.emptyMap();
        }
    }
}
