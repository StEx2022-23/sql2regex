package sqltoregex.deparser;

import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class StatementDeParser extends net.sf.jsqlparser.util.deparser.StatementDeParser {
    public StatementDeParser(StringBuilder buffer) {
        super(buffer);
    }

    public StatementDeParser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }

    public StatementDeParser(ExpressionDeParser expressionDeParser, StringBuilder buffer) {
        super(expressionDeParser, new sqltoregex.deparser.SelectDeParser(), buffer);
    }

}
