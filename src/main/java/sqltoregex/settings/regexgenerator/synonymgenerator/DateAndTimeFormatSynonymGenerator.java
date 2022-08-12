package sqltoregex.settings.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * @param expr expression to parse
     * @return parsed SimpleDateFormat
     * @throws NoSuchElementException if there are no synonym formats
     */
    @Override
    protected SimpleDateFormat prepareSynonymForSearch(Expression expr) {
        Assert.notNull(expr, SYNONYM_MUST_NOT_BE_NULL);
        if (!(expr instanceof DateValue) && !(expr instanceof TimeValue) && !(expr instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }

        String stringToCheck = DateAndTimeFormatSynonymGenerator.expressionToString(expr);
        List<SimpleDateFormat> sortedFormats = this.synonymsGraph.vertexSet().stream().sorted((v1, v2) -> v2.toPattern().length() - v1.toPattern().length()).toList();

        for (SimpleDateFormat vertexSyn : sortedFormats) {
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
     * If the provided Expression is parsable by any format in the graph it is further formatted by the provided SimpleDateFormat.
     * @param syn the specified {@link SimpleDateFormat}
     * @param expr the item to find synonyms for as {@link Expression}
     * @return Expression formated by provided SimpleDateFormat
     * @throws NoSuchElementException if there are no synonym formats
     */
    @Override
    protected String prepareVertexForRegEx(SimpleDateFormat syn, Expression expr) {
        Assert.notNull(syn, "Format must not be null");
        Assert.notNull(expr, SYNONYM_MUST_NOT_BE_NULL);

        try {
            //O(n^2) is okay, cause elements will be <<100 for dateFormats
            Date date = this.prepareSynonymForSearch(expr).parse(DateAndTimeFormatSynonymGenerator.expressionToString(expr));
            return syn.format(date);
        } catch (ParseException e) {
            throw new UnsupportedOperationException("prepareVertexForRegEx called outside of SynonymGenerator.generateAsList()");
        }
    }

    /**
     * Finds a Synonym for a passed {@link Expression} with the item to find synonyms for.
     * @param expr the item to find synonyms for as {@link Expression}
     * @return found synonym as string
     * @throws IllegalArgumentException if the {@link Expression} isn't instanceof {@link DateValue}, {@link TimeValue} or {@link TimestampValue}
     */
    @Override
    public String searchSynonymToString(Expression expr) {
        Assert.notNull(expr, SYNONYM_MUST_NOT_BE_NULL);
        if (!(expr instanceof DateValue) && !(expr instanceof TimeValue) && !(expr instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }
        return DateAndTimeFormatSynonymGenerator.expressionToString(expr);
    }

    /**
     * Converts an expression to the string equivalent.
     * @param expr to parsed expression
     * @return expression as string
     * @throws IllegalArgumentException if the {@link Expression} isn't instanceof {@link DateValue}, {@link TimeValue} or {@link TimestampValue}
     */
    public static String expressionToString(Expression expr) {
        Assert.notNull(expr, SYNONYM_MUST_NOT_BE_NULL);
        if (!(expr instanceof DateValue) && !(expr instanceof TimeValue) && !(expr instanceof TimestampValue)) {
            throw new IllegalArgumentException(MUST_BE_OF_TYPE_DATE_TIME_TIMESTAMP_VALUE);
        }

        if (expr instanceof  DateValue dateValue){
            return dateValue.getRawValue();
        }else if(expr instanceof TimeValue timeValue){
            return timeValue.getValue().toString();
        }else if(expr instanceof TimestampValue timestampValue){
            return timestampValue.getRawValue();
        }else{
            return expr.toString();
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
     * @param expr input expr
     * @return generated list of synonyms as string or given string as one entry in the string list
     */
    public static List<String> generateAsListOrDefault(DateAndTimeFormatSynonymGenerator synonymGenerator, Expression expr){
        if (null != synonymGenerator) return synonymGenerator.generateAsList(expr);
        return new LinkedList<>(List.of(DateAndTimeFormatSynonymGenerator.expressionToString(expr)));
    }
}
