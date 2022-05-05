package sqltoregex.property;

import net.sf.jsqlparser.expression.DateValue;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

//TODO: add common präfix and suffix for the or concatenation to make something like: \s*|\s*
abstract class SynonymManager<A, S> {
    //due to: Edges undirected (synonyms apply in both directions); Self-loops: no; Multiple edges: no; weighted: yes
    protected SimpleWeightedGraph<A, DefaultWeightedEdge> synonymsGraph;
    private String prefix = "";
    private String suffix = "";

    protected SynonymManager() {
        this.synonymsGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public boolean addSynonym(A syn) {
        return addSynonym(syn, 1L);
    }

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
            return wordToFindSynonyms.toString();
        }
    }

    /**
     * Preprocessed input into a String that will be stored in the graph.
     *
     * @param syn
     * @return
     */
    protected abstract A prepareSynonymForAdd(A syn);

    /**
     * Preprocesses input for search.
     *
     * @param wordToFindSynonyms
     * @return
     */
    protected abstract A prepareSynonymForSearch(S wordToFindSynonyms);

    protected abstract String prepareVertexForRegEx(A syn, S wordToFindSynonyms);

    public boolean removeSynonym(A syn) {
        return synonymsGraph.removeVertex(this.prepareSynonymForAdd(syn));
    }

    /**
     * Sets a common prefix for all concatenations of {@link #generateSynonymRegexFor}
     * @param prefix prefix to add
     */
    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    /**
     * Sets a common suffix for all concatenations of {@link #generateSynonymRegexFor}
     * @param suffix suffix to add
     */
    public void setSuffix(String suffix){
        this.suffix = suffix;
    }
}


//TODO: extract Date, Time and Datetime into a seperate class, since there are separate Expressions for it.

/**
 * Generate Synonym goes through all saved formats and tries to parse. If it parses successfully it will generate
 * Dates with all Formats the matched Vertex pointed to.
 */
class DateSynonymManager extends SynonymManager<DateFormat, DateValue> {
    /**
     * check against allowed patterns and throw IllegalArgumentException if not
     *
     * @param syn
     * @return
     */
    @Override
    protected DateFormat prepareSynonymForAdd(DateFormat syn) {
        return syn;
    }

    @Override
    protected DateFormat prepareSynonymForSearch(DateValue wordToFindSynonyms) {
        for (DateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                vertexSyn.parse(wordToFindSynonyms.getValue().toString());
                return vertexSyn;
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }

    @Override
    protected String prepareVertexForRegEx(DateFormat syn, DateValue wordToFindSynonyms) {
        Date date;

        //O(n^2) is okay, cause elements will be <<100 for dateFormats
        for (DateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                date = vertexSyn.parse(wordToFindSynonyms.getValue().toString());
                return syn.format(date);
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }
}

/**
 * Default implementation of {@link SynonymManager}. Saves Strings and searches with exact representation of them.
 * e.g. used for: Data-Type synonyms
 */
class DefaultSynonymManager extends SynonymManager<String, String> {

    @Override
    protected String prepareSynonymForAdd(String syn) {
        return syn;
    }

    @Override
    protected String prepareSynonymForSearch(String wordToFindSynonyms) {
        return wordToFindSynonyms;
    }

    @Override
    protected String prepareVertexForRegEx(String syn, String wordToFindSynonyms) {
        return syn;
    }
}