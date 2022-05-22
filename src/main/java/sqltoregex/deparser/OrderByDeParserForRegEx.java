package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;

public class OrderByDeParserForRegEx extends OrderByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private ExpressionVisitor regExExpressionVisitor;

    public OrderByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(expressionVisitor, buffer);
        this.regExExpressionVisitor = expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor expressionVisitor){
        this.regExExpressionVisitor = expressionVisitor;
    }

    public ExpressionVisitor getExpressionVisitor(){
        return this.regExExpressionVisitor;
    }

    @Override
    public void deParseElement(OrderByElement orderBy) {
        orderBy.getExpression().accept(this.regExExpressionVisitor);
        if (!orderBy.isAsc()) {
            this.buffer.append(OPTIONAL_WHITE_SPACE).append("DESC");
        } else if (orderBy.isAscDescPresent()) {
            this.buffer.append(OPTIONAL_WHITE_SPACE).append("ASC");
        }

        if (orderBy.getNullOrdering() != null) {
            this.buffer.append(REQUIRED_WHITE_SPACE);
            this.buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }

    }
}
