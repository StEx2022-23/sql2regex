package sqltoregex;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.springframework.stereotype.Service;
import sqltoregex.property.DateAndTimeSynonymGenerator;

@Service
public class SynonymManager {
    private DateAndTimeSynonymGenerator dateSynonymGenerator;
    private DateAndTimeSynonymGenerator timeSynonymGenerator;
    private DateAndTimeSynonymGenerator timestampSynonymGenerator;

    public SynonymManager() {
        //TODO: should be set to default all Options from XML and further be manipulated over the setter methods at
        // runtime
    }

    public String generateSynonymRegexFor(Expression expr) {
        SynonymExpressionDeParser synonymExpressionDeParser = new SynonymExpressionDeParser();
        expr.accept(synonymExpressionDeParser);
        return synonymExpressionDeParser.getBuffer().toString();
    }

    public void setDateSynonymGenerator(DateAndTimeSynonymGenerator dateSynonymGenerator) {
        this.dateSynonymGenerator = dateSynonymGenerator;
    }

    public void setTimeSynonymGenerator(DateAndTimeSynonymGenerator timeSynonymGenerator) {
        this.timeSynonymGenerator = timeSynonymGenerator;
    }

    public void setTimestampSynonymGenerator(DateAndTimeSynonymGenerator timestampSynonymGenerator) {
        this.timestampSynonymGenerator = timestampSynonymGenerator;
    }

    private class SynonymExpressionDeParser extends ExpressionDeParser {
        @Override
        public void visit(DateValue dateValue) {
            getBuffer().append(dateSynonymGenerator.generateSynonymRegexFor(dateValue));
        }

        @Override
        public void visit(TimeValue timeValue) {
            getBuffer().append(timeSynonymGenerator.generateSynonymRegexFor(timeValue));
        }

        @Override
        public void visit(TimestampValue timestampValue) {
            getBuffer().append(timestampSynonymGenerator.generateSynonymRegexFor(timestampValue));
        }
    }
}
