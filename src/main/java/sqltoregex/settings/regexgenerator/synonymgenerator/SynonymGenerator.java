package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.RegExGenerator;

import java.util.*;

/**
 * Common Interface for all synonym managers. Provides functionality for adding, removing synonyms of a generic Type T.
 * Generates RegEx with TemplateMethod generateSynonymRegexFor with implementations of preprocessor in concrete classes.
 * With {@link #prepareSynonymForAdd(Object)} and {@link #prepareSynonymForSearch(Object)}. It's possible to use a
 * different syntax for Vertexes and SearchTerms as it's needed for Dates.
 * <p>
 * All of these methods are modeled on the java.util collections framework,accept for accessing so:
 * <p>
 * Adds a duplicate object to a set (e.g. when adding a vertex) is not an error, but the duplicate is discarded.
 * Attempts to remove an object which was not part of the graph is not an error.
 * But attempting to access an object which is not part of the graph is not allowed. Then there is just the synonym
 * query returned.
 *
 * @param <A> class of added objects and class in which the synonym graph is built.
 * @param <S> class of search objects
 */

public abstract class SynonymGenerator<A, S> extends RegExGenerator<S> {
    public static final long DEFAULT_WEIGHT = 1L;
    //due to: Edges undirected (synonyms apply in both directions); Self-loops: no; Multiple edges: no; weighted: yes
    protected SimpleWeightedGraph<A, DefaultWeightedEdge> synonymsGraph;
    protected boolean graphForSynonymsOfTwoWords = false;
    private String prefix = "";
    private String suffix = "";


    /**
     * Generates a list of strings with the searched item and the related synonyms.
     * Implements generateAsList method from {@link sqltoregex.settings.regexgenerator.IRegExGenerator}.
     * @param wordToFindSynonyms item to find synonyms for
     * @return list of string with the searched item and the related synonyms
     */
    @Override
    public List<String> generateAsList(S wordToFindSynonyms) {
        List<String> stringList = new LinkedList<>();
        try {
            A vertexToSearch = this.prepareSynonymForSearch(wordToFindSynonyms);
            A start = this.synonymsGraph.vertexSet().stream()
                    .filter(syn -> syn.equals(vertexToSearch)).findAny().get();

            Iterator<A> iterator = new DepthFirstIterator<>(synonymsGraph, start);
            while (iterator.hasNext()) {
                stringList.add(this.prefix + this.prepareVertexForRegEx(iterator.next(), wordToFindSynonyms) + this.suffix);

            }
            return stringList;
        } catch (NoSuchElementException e) {
            stringList.add(searchSynonymToString(wordToFindSynonyms));
            return stringList;
        }
    }

    /**
     * SynonymGenerator constructor. Need one of enum {@link SettingsOption}.
     * Inits the super class {@link RegExGenerator}.
     * Inits a {@link SimpleWeightedGraph}.
     * @param settingsOption one of enum {@link SettingsOption}
     * @see SettingsOption
     */
    protected SynonymGenerator(SettingsOption settingsOption) {
        super(settingsOption);
        this.synonymsGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Adds a synonym to all prior existing synonyms with default weight after {@link #prepareSynonymForAdd}.
     * @param syn synonym
     * @return boolean for add operation success
     */
    public boolean addSynonym(A syn) {
        return addSynonym(syn, DEFAULT_WEIGHT);
    }

    /**
     * Adds a synonym to all prior existing synonyms with custom weight after {@link #prepareSynonymForAdd}.
     * @param syn synonym
     * @param weight specific weight in the graph
     * @return boolean for add operation success
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
     * Adds a synonym to a second given synonym with default weight after {@link #prepareSynonymForAdd}.
     * @param syn base synonym
     * @param synFor new synonym
     * @return boolean for add operation success
     */
    public boolean addSynonymFor(A syn, A synFor) {
        return addSynonymFor(syn, synFor, DEFAULT_WEIGHT);
    }

    /**
     * Adds a synonym to a second given synonym with custom weight after {@link #prepareSynonymForAdd}.
     * @param syn base synonym
     * @param synFor new synonym
     * @param weight specific weight in the graph
     * @return boolean for add operation success
     */
    public boolean addSynonymFor(A syn, A synFor, Long weight) {
        this.graphForSynonymsOfTwoWords = true;
        boolean addResult = false;
        if (!synonymsGraph.containsVertex(syn)) {
            addResult = synonymsGraph.addVertex(this.prepareSynonymForAdd(syn));
        }
        if (!synonymsGraph.containsVertex(synFor)) {
            addResult = synonymsGraph.addVertex(this.prepareSynonymForAdd(synFor)) || addResult;
        }
        if (addResult) {
            DefaultWeightedEdge e1 = synonymsGraph.addEdge(this.prepareSynonymForAdd(syn),
                                                           this.prepareSynonymForAdd(synFor));
            synonymsGraph.setEdgeWeight(e1, weight);
            return true;
        }
        return false;
    }

    /**
     * Overrides default equals method. Not using graph equals method cause wrong implementation in <a href="https://jgrapht.org/">JGRAPHT</a>>.
     * @param o to compare object
     * @return boolean for comparison of the two objects
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SynonymGenerator<?, ?> that)) return false;
        return this.synonymsGraph.vertexSet().equals(that.synonymsGraph.vertexSet())
                && this.synonymsGraph.edgeSet().size() == that.synonymsGraph.edgeSet().size()
                && this.prefix.equals(that.prefix)
                && this.suffix.equals(that.suffix)
                && this.getSettingsOption() == that.getSettingsOption()
                && this.isNonCapturingGroup == that.isNonCapturingGroup;
    }

    public Graph<A, DefaultWeightedEdge> getGraph() {
        try {
            return (Graph<A, DefaultWeightedEdge>) this.synonymsGraph.clone();
        } catch (ClassCastException exception) {
            return null;
        }
    }

    /**
     * Overrides default hashCode() method. Uses all attributes of the class {@link SynonymGenerator}.
     * @return hashcode as int
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                this.synonymsGraph.vertexSet(),
                this.synonymsGraph.edgeSet().size(),
                this.prefix,
                this.suffix,
                this.getSettingsOption(),
                this.isNonCapturingGroup
        );
    }

    /**
     * Preprocessed input into a String that will be stored in the graph.
     * @param syn
     * @return
     */
    protected abstract A prepareSynonymForAdd(A syn);

    /**
     * Preprocesses input for search. Changing the search class into the vertex class.
     * @param wordToFindSynonyms
     * @return
     */
    protected abstract A prepareSynonymForSearch(S wordToFindSynonyms);

    /**
     * Makes it possible to manipulate the way a synonym class is converted into a {@literal String} for RegEx usage
     * without changing {@link Object#toString}. Especially useful when A and S generics are different.
     * @param syn
     * @param wordToFindSynonyms
     * @return
     */
    protected abstract String prepareVertexForRegEx(A syn, S wordToFindSynonyms);

    /**
     * Removes a synonym from the graph.
     * @param syn the selected synonym
     * @return boolean for remove operation success
     */
    public boolean removeSynonym(A syn) {
        return synonymsGraph.removeVertex(this.prepareSynonymForAdd(syn));
    }

    /**
     * Converts input to a string.
     * @param wordToFindSynonyms input of unspecified type
     * @return input as string
     */
    public String searchSynonymToString(S wordToFindSynonyms) {
        return wordToFindSynonyms.toString();
    }

    /**
     * Sets a common prefix for all concatenations of {@link #generateRegExFor}.
     * @param prefix prefix to add
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Sets a common suffix for all concatenations of {@link #generateRegExFor}.
     * @param suffix suffix to add
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}