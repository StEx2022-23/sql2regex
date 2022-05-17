package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;

public class OrderByDeParserForRegEx extends OrderByDeParser {
    private ExpressionVisitor expressionVisitor;

    public OrderByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(expressionVisitor, buffer);
    }

    public void setExpressionVisitor(ExpressionVisitor expressionVisitor){
        this.expressionVisitor = expressionVisitor;
    }
}
