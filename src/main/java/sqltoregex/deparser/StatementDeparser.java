package sqltoregex.deparser;

import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class StatementDeparser extends StatementDeParser {
    public StatementDeparser(StringBuilder buffer) {
        super(buffer);
    }

    public StatementDeparser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }

    public StatementDeparser(ExpressionDeParser expressionDeParser, StringBuilder buffer) {
        super(expressionDeParser, new SelectDeParser(), buffer);
    }

}
