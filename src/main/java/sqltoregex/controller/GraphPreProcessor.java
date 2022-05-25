package sqltoregex.controller;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphPreProcessor {

    public static final String COULD_NOT_CAST = "Could not cast";

    private GraphPreProcessor() {

    }

    static <T> Set<T> getSynonymSet(Graph<T, DefaultWeightedEdge> graph) {
        try {
            return new LinkedHashSet<>(graph.vertexSet());
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, COULD_NOT_CAST, e);
            return Collections.emptySet();
        }
    }

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

    static <T> Map<T, Set<T>> getSynonymMap(Graph<T, DefaultWeightedEdge> graph) {
        try {
            Map<T, Set<T>> map = new HashMap<>();
            Set<T> vertexSet = new HashSet<>(graph.vertexSet());
            Set<T> walkedVertices = new HashSet<>();
            for (T startVertex : vertexSet) {
                if (walkedVertices.contains(startVertex)) {
                    continue;
                }
                Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(startVertex);
                Set<T> endVertexSet = new LinkedHashSet<>();
                for (DefaultWeightedEdge edge : outgoingEdges) {
                    T target = graph.getEdgeTarget(edge) == startVertex ? graph.getEdgeSource(
                            edge) : graph.getEdgeTarget(edge);
                    endVertexSet.add(target);
                    walkedVertices.add(target);
                }
                map.put(startVertex, endVertexSet);
            }
            return map;
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, COULD_NOT_CAST, e);
            return Collections.emptyMap();
        }
    }
}
