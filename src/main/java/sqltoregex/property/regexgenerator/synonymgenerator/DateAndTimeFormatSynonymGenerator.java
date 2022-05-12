package sqltoregex.property.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import sqltoregex.property.PropertyOption;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

public class DateAndTimeFormatSynonymGenerator extends SynonymGenerator<SimpleDateFormat, Expression> {
    public DateAndTimeFormatSynonymGenerator(PropertyOption propertyOption) {
        super(propertyOption);
    }

    @Override
    protected SimpleDateFormat prepareSynonymForAdd(SimpleDateFormat syn) {
        return syn;
    }

    @Override
    protected SimpleDateFormat prepareSynonymForSearch(Expression wordToFindSynonyms) {
        if(!(wordToFindSynonyms instanceof DateValue) && !(wordToFindSynonyms instanceof TimeValue) && !(wordToFindSynonyms instanceof TimestampValue)){
            throw new IllegalArgumentException("Expression for DateAndTimeSynonymManager must be of type: DateValue, TimeValue or TimestampValue");
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
        DateAndTimeExpressionDeparser deParser = new DateAndTimeExpressionDeparser();
        wordToFindSynonyms.accept(deParser);
        return deParser.getBuffer().toString();
    }

    class DateAndTimeExpressionDeparser extends ExpressionDeParser {
        @Override
        public void visit(DateValue value) {
            getBuffer().append(value.getValue());
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
