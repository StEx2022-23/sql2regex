package sqltoregex.settings.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * SynonymGenerator for dateformats, timeformats and datetimeformats. Needs SimpleDateFormat and Expression as type.
 */
public class DateAndTimeFormatSynonymGenerator extends SynonymGenerator<SimpleDateFormat, Expression> {
    public static final String SYNONYM_MUST_NOT_BE_NULL = "Synonym must not be null";
    public static final String MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE = "Query Expression must be of type " +
            "Date-/Time-/TimestampValue";

    /**
     * Constructor of DateAndTimeFormatSynonymGenerator. Inits the super class.
     * @param settingsOption one of enum {@link SettingsOption}
     */
    public DateAndTimeFormatSynonymGenerator(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * Generates a regular expression for the {@link SimpleDateFormat} synonyms.
     * Checks if given value is instanceof {@link DateValue}, {@link TimeValue} or {@link TimestampValue}
     * @param value value to handle
     * @return generated regex
     */
    @Override
    public String generateRegExFor(Expression value) {
        if (!(value instanceof DateValue) && !(value instanceof TimeValue) && !(value instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }
        return super.generateRegExFor(value);
    }

    /**
     * Prepares {@link SimpleDateFormat} for using with {@link SimpleDateFormat}.setLenient(false)
     * @param syn SimpleDateFormat
     * @return parsed SimpleDateFormat
     */
    @Override
    protected SimpleDateFormat prepareSynonymForAdd(SimpleDateFormat syn) {
        Assert.notNull(syn, "Format must not be null");
        syn.setLenient(false);
        return syn;
    }

    /**
     * Parses expression to a {@link SimpleDateFormat}.
     * @param wordToFindSynonyms expression to parse
     * @return parsed SimpleDateFormat
     * @throws NoSuchElementException if there are no synonym formats
     */
    @Override
    protected SimpleDateFormat prepareSynonymForSearch(Expression wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);
        if (!(wordToFindSynonyms instanceof DateValue) && !(wordToFindSynonyms instanceof TimeValue) && !(wordToFindSynonyms instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }

        String stringToCheck = DateAndTimeFormatSynonymGenerator.expressionToString(wordToFindSynonyms);

        for (SimpleDateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                vertexSyn.parse(stringToCheck);
                return vertexSyn;
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }

    /**
     * Prepares vertex by passing a {@link SimpleDateFormat} and a {@link Expression} with the item to find synonyms for.
     * @param syn the specified {@link SimpleDateFormat}
     * @param wordToFindSynonyms  the item to find synonyms for as {@link Expression}
     * @return parsed string
     * @throws NoSuchElementException if there are no synonym formats
     */
    @Override
    protected String prepareVertexForRegEx(SimpleDateFormat syn, Expression wordToFindSynonyms) {
        Assert.notNull(syn, "Format must not be null");
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);

        Date date;
        //O(n^2) is okay, cause elements will be <<100 for dateFormats
        for (DateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                date = vertexSyn.parse(DateAndTimeFormatSynonymGenerator.expressionToString(wordToFindSynonyms));
                return syn.format(date);
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }

    /**
     * Finds a Synonym for a passed {@link Expression} with the item to find synonyms for.
     * @param wordToFindSynonyms the item to find synonyms for as {@link Expression}
     * @return found synonym as string
     * @throws IllegalArgumentException if the {@link Expression} isn't instanceof {@link DateValue}, {@link TimeValue} or {@link TimestampValue}
     */
    @Override
    public String searchSynonymToString(Expression wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);
        if (!(wordToFindSynonyms instanceof DateValue) && !(wordToFindSynonyms instanceof TimeValue) && !(wordToFindSynonyms instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }
        return DateAndTimeFormatSynonymGenerator.expressionToString(wordToFindSynonyms);
    }

    /**
     * Converts an expression to the string equivalent.
     * @param wordToFindSynonyms to parsed expression
     * @return expression as string
     * @throws IllegalArgumentException if the {@link Expression} isn't instanceof {@link DateValue}, {@link TimeValue} or {@link TimestampValue}
     */
    public static String expressionToString(Expression wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);
        if (!(wordToFindSynonyms instanceof DateValue) && !(wordToFindSynonyms instanceof TimeValue) && !(wordToFindSynonyms instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }

        if (wordToFindSynonyms instanceof  DateValue dateValue){
            return dateValue.getRawValue();
        }else if(wordToFindSynonyms instanceof TimeValue timeValue){
            return timeValue.getValue().toString();
        }else if(wordToFindSynonyms instanceof TimestampValue timestampValue){
            return timestampValue.getRawValue();
        }else{
            return wordToFindSynonyms.toString();
        }
    }

    /**
     * Generates a regex with all possible synonyms, if the param synonymGenerator isn't null.
     * @param synonymGenerator {@link DateAndTimeFormatSynonymGenerator} object
     * @param expression input expression
     * @return generated regex or str
     */
    public static String useOrDefault(DateAndTimeFormatSynonymGenerator synonymGenerator, Expression expression){
        if (null != synonymGenerator) return synonymGenerator.generateRegExFor(expression);
        else return DateAndTimeFormatSynonymGenerator.expressionToString(expression);
    }

    /**
     * Generates a list of strings with all possible synonyms, if the param synonymGenerator isn't null.
     * @param synonymGenerator {@link DateAndTimeFormatSynonymGenerator} object
     * @param expression input expression
     * @return generated list of synonyms as string or given string as one entry in the string list
     */
    public static List<String> generateAsListOrDefault(DateAndTimeFormatSynonymGenerator synonymGenerator, Expression expression){
        if (null != synonymGenerator) return synonymGenerator.generateAsList(expression);
        return new LinkedList<>(List.of(DateAndTimeFormatSynonymGenerator.expressionToString(expression)));
    }
}
