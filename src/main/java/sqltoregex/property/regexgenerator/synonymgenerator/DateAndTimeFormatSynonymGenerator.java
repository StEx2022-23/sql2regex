package sqltoregex.property.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.springframework.util.Assert;
import sqltoregex.property.PropertyOption;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

public class DateAndTimeFormatSynonymGenerator extends SynonymGenerator<SimpleDateFormat, Expression> {
    public static final String SYNONYM_MUST_NOT_BE_NULL = "Synonym must not be null";

    public DateAndTimeFormatSynonymGenerator(PropertyOption propertyOption) {
        super(propertyOption);
    }

    @Override
    public String generateRegExFor(Expression wordToFindSynonyms) {
        if (wordToFindSynonyms.getClass() != DateValue.class && wordToFindSynonyms.getClass() != TimeValue.class && wordToFindSynonyms.getClass() != TimestampValue.class) {
            throw new IllegalArgumentException("Query Expression must be of type Date-/Time-/TimestampValue");
        }
        return super.generateRegExFor(wordToFindSynonyms);
    }

    @Override
    protected SimpleDateFormat prepareSynonymForAdd(SimpleDateFormat syn) {
        Assert.notNull(syn, "Format must not be null");
        return syn;
    }

    @Override
    protected SimpleDateFormat prepareSynonymForSearch(Expression wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);
        if (!(wordToFindSynonyms instanceof DateValue) && !(wordToFindSynonyms instanceof TimeValue) && !(wordToFindSynonyms instanceof TimestampValue)) {
            throw new IllegalArgumentException(
                    "Expression for DateAndTimeSynonymManager must be of type: DateValue, TimeValue or TimestampValue");
        }

        for (SimpleDateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                DateAndTimeExpressionDeparser deParser = new DateAndTimeExpressionDeparser();
                wordToFindSynonyms.accept(deParser);
                vertexSyn.parse(deParser.getBuffer().toString());
                return vertexSyn;
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }

    @Override
    protected String prepareVertexForRegEx(SimpleDateFormat syn, Expression wordToFindSynonyms) {
        Assert.notNull(syn, "Format must not be null");
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);

        Date date;
        //O(n^2) is okay, cause elements will be <<100 for dateFormats
        for (DateFormat vertexSyn : this.synonymsGraph.vertexSet()) {
            try {
                DateAndTimeExpressionDeparser deParser = new DateAndTimeExpressionDeparser();
                wordToFindSynonyms.accept(deParser);
                date = vertexSyn.parse(deParser.getBuffer().toString());
                return syn.format(date);
            } catch (ParseException e) {
                //continue to search for other possible patterns without throwing error.
            }
        }
        throw new NoSuchElementException("There are no synonym formats for the entered date format");
    }

    @Override
    public String searchSynonymToString(Expression wordToFindSynonyms) {
        if (wordToFindSynonyms.getClass() != DateValue.class && wordToFindSynonyms.getClass() != TimeValue.class && wordToFindSynonyms.getClass() != TimestampValue.class) {
            throw new IllegalArgumentException("Query Expression must be of type Date-/Time-/TimestampValue");
        }
        Assert.notNull(wordToFindSynonyms, SYNONYM_MUST_NOT_BE_NULL);
        DateAndTimeExpressionDeparser deParser = new DateAndTimeExpressionDeparser();
        wordToFindSynonyms.accept(deParser);
        return deParser.getBuffer().toString();
    }

    class DateAndTimeExpressionDeparser extends ExpressionDeParser {
        @Override
        public void visit(DateValue value) {
            getBuffer().append(value.getRawValue());
        }

        @Override
        public void visit(TimeValue value) {
            this.setBuffer(new StringBuilder());
            getBuffer().append(value.getValue());
        }

        @Override
        public void visit(TimestampValue value) {
            this.setBuffer(new StringBuilder());
            getBuffer().append(value.getRawValue());
        }
    }
}
