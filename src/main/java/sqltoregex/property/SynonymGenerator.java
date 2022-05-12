package sqltoregex.property;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

/**
 * Common Interface for all synonym managers. Provides functionality for adding, removing synonyms of a generic Type T.
 * Generates RegEx with TemplateMethod generateSynonymRegexFor with implementations of preprocessor in concrete classes.
 * With {@link #prepareSynonymForAdd(Object)} and {@link #prepareSynonymForSearch(Object)} It's possible to use a
 * different syntax for Vertexes and SearchTerms as it's needed for Dates.
 * <p>
 * All of these methods are modeled on the java.util collections framework,accept for accessing so:
 * <p>
 * adding a duplicate object to a set (e.g. when adding a vertex) is not an error, but the duplicate is discarded
 * attempting to remove an object which was not part of the graph is not an error
 * but attempting to access an object which is not part of the graph is not allowed. Then there is just the synonym
 * query returned.
 *
 * @param <A> class of added objects and class in which the synonym graph is built.
 * @param <S> class of search objects
 */

//SimpleSynonymGenerator 1 abstrakte Klasse added synonym für alle in einem ungerichteten graphen
//directedSynonym Generator 1 abstrakte Klasse added Synonym für spezielle Wörter in dem Graphen
//beide implementieren das Interface RegExSynonymGenerator → das hat nur die Methode generateSynonymRegexFor und prepareSynonymForSearch und prepareSynonymForAdd


abstract class SynonymGenerator<A, S> implements Property<A> {
    //due to: Edges undirected (synonyms apply in both directions); Self-loops: no; Multiple edges: no; weighted: yes
    protected SimpleWeightedGraph<A, DefaultWeightedEdge> synonymsGraph;
    private String prefix = "";
    private String suffix = "";

    protected SynonymGenerator() {
        this.synonymsGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Add a synonym with default weight.
     *
     * @param syn
     * @return
     */
    public boolean addSynonym(A syn) {
        return addSynonym(syn, 1L);
    }

    /**
     * Add a synonym with custom weight after {@link #prepareSynonymForAdd}
     *
     * @param syn
     * @param weight
     * @return
     */
    public boolean addSynonym(A syn, Long weight) {
        if (synonymsGraph.addVertex(this.prepareSynonymForAdd(syn))) {
            for (A next : this.synonymsGraph.vertexSet()) {
                if (next != syn) {
                    DefaultWeightedEdge e1 = synonymsGraph.addEdge(this.prepareSynonymForAdd(syn), next);
                    synonymsGraph.setEdgeWeight(e1, weight);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Generates a regular expression part String with the pre-/ and suffixes set <b>including</b> the param.
     *
     * @param wordToFindSynonyms
     * @return
     */
    public String generateSynonymRegexFor(S wordToFindSynonyms) {
        try {
            A vertexToSearch = this.prepareSynonymForSearch(wordToFindSynonyms);
            A start = this.synonymsGraph.vertexSet().stream()
                    .filter(syn -> syn.equals(vertexToSearch)).findAny().get();

            Iterator<A> iterator = new DepthFirstIterator<>(synonymsGraph, start);
            StringBuilder strRegEx = new StringBuilder();
            while (iterator.hasNext()) {
                strRegEx.append(prefix);
                strRegEx.append(prepareVertexForRegEx(iterator.next(), wordToFindSynonyms));
                strRegEx.append(suffix);
                strRegEx.append("|");
            }
            strRegEx.deleteCharAt(strRegEx.length() - 1);
            return strRegEx.toString();
        } catch (NoSuchElementException e) {
            return searchSynonymToString(wordToFindSynonyms);
        }
    }

//    private Iterator<A> getIteratorStartingFrom(S wordToFindSynonyms){
//        A vertexToSearch = this.prepareSynonymForSearch(wordToFindSynonyms);
//        A start = this.synonymsGraph.vertexSet().stream()
//                .filter(syn -> syn.equals(vertexToSearch)).findAny().get();
//        return new DepthFirstIterator<>(synonymsGraph, start);
//    }
//
//    public Collection<A> getSynonymsCollectionFor(S wordToFindSynonyms){
//        Collection<A> synonymsCollection = new HashSet<>();
//        try {
//            Iterator<A> iterator = getIteratorStartingFrom(wordToFindSynonyms);
//            while (iterator.hasNext()) {
//                synonymsCollection.add(iterator.next());
//            }
//        } catch (NoSuchElementException e) {
//            synonymsCollection.add(this.prepareSynonymForSearch(wordToFindSynonyms));
//        }
//        return synonymsCollection;
//    }

    @Override
    public Set<A> getSettings() {
        return synonymsGraph.vertexSet();
    }

    /**
     * Preprocessed input into a String that will be stored in the graph.
     *
     * @param syn
     * @return
     */
    protected abstract A prepareSynonymForAdd(A syn);

    /**
     * Preprocesses input for search. Changing the search class into the vertex class.
     *
     * @param wordToFindSynonyms
     * @return
     */
    protected abstract A prepareSynonymForSearch(S wordToFindSynonyms);

    /**
     * Makes it possible to manipulate the way a synonym class is converted into a {@literal String} for RegEx usage
     * without changing {@link Object#toString}. Especially useful when A and S generics are different.
     *
     * @param syn
     * @param wordToFindSynonyms
     * @return
     */
    protected abstract String prepareVertexForRegEx(A syn, S wordToFindSynonyms);

    /**
     * Removes a synonym from the graph.
     * @param syn
     * @return
     */
    public boolean removeSynonym(A syn) {
        return synonymsGraph.removeVertex(this.prepareSynonymForAdd(syn));
    }

    /**
     * Converts the search synonym to a string.
     *
     * @param wordToFindSynonyms
     * @return
     */
    public String searchSynonymToString(S wordToFindSynonyms) {
        return wordToFindSynonyms.toString();
    }

    /**
     * Sets a common prefix for all concatenations of {@link #generateSynonymRegexFor}
     *
     * @param prefix prefix to add
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Sets a common suffix for all concatenations of {@link #generateSynonymRegexFor}
     *
     * @param suffix suffix to add
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}