package sqltoregex.property;

import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimestampValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.NoSuchElementException;

public class TimestampValueSynonymManager extends SynonymManager<DateFormat, TimestampValue> {
    @Override
    protected DateFormat prepareSynonymForAdd(DateFormat syn) {
        return syn;
    }

    @Override
    protected DateFormat prepareSynonymForSearch(TimestampValue wordToFindSynonyms) {

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
    protected String prepareVertexForRegEx(DateFormat syn, TimestampValue wordToFindSynonyms) {
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

    @Override
    public String searchSynonymToString(TimestampValue wordToFindSynonyms) {
        return wordToFindSynonyms.getRawValue().toString();
    }
}
