package sqltoregex.deparser;

import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class StatementDeParserForRegEx extends StatementDeParser {
    public StatementDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }

    public StatementDeParserForRegEx(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }

    public StatementDeParserForRegEx(ExpressionDeParser expressionDeParser, StringBuilder buffer) {
        super(expressionDeParser, new SelectDeParserForRegEx(), buffer);
    }

}
