package sqltoregex.controller;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphPreProcessor {

    private GraphPreProcessor(){

    }

    static <T> Set<T> getSynonymSet(Graph<T,DefaultWeightedEdge> graph){
        try {
            return new LinkedHashSet<>(graph.vertexSet());
        }catch (ClassCastException e){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, "Could not cast", e);
            return Collections.emptySet();
        }
    }

    static <T> Map<T, List<T>> getSynonymMap(Graph<T,DefaultWeightedEdge> graph){
        try {
            Map<T, List<T>> map = new HashMap<>();
            Set<T> vertexSet = new HashSet<>(graph.vertexSet());
            Set<T> walkedVertices = new HashSet<>();
            for (T startVertex : vertexSet){
                if (walkedVertices.contains(startVertex)){
                    continue;
                }
                Set<DefaultWeightedEdge> outgoingEdges =  graph.outgoingEdgesOf(startVertex);
                List<T> endVertexList = new LinkedList<>();
                for (DefaultWeightedEdge edge : outgoingEdges){
                    T target = graph.getEdgeTarget(edge) == startVertex ? graph.getEdgeSource(edge) : graph.getEdgeTarget(edge);
                    endVertexList.add(target);
                    walkedVertices.add(target);
                }
                map.put(startVertex, endVertexList);
            }
            return map;
        }catch (ClassCastException e){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, "Could not cast", e);
            return Collections.emptyMap();
        }
    }
}
