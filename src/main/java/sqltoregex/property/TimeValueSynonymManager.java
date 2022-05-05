package sqltoregex.property;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimeValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * Generate Synonym goes through all saved formats and tries to parse. If it parses successfully it will generate
 * Dates with all Formats the matched Vertex pointed to.
 */
class TimeValueSynonymManager extends SynonymManager<DateFormat, TimeValue> {
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
    protected DateFormat prepareSynonymForSearch(TimeValue wordToFindSynonyms) {
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
    protected String prepareVertexForRegEx(DateFormat syn, TimeValue wordToFindSynonyms) {
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