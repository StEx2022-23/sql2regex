package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    public GroupByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(expressionVisitor, buffer);
    }
}
