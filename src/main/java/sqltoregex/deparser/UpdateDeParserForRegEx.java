package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;

public class UpdateDeParserForRegEx extends UpdateDeParser {
    public UpdateDeParserForRegEx() {
        super();
    }

    public UpdateDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(expressionVisitor, buffer);
    }

    @Override
    public void deParse(Update update) {
        super.deParse(update);
    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return super.getExpressionVisitor();
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        super.setExpressionVisitor(visitor);
    }

    @Override
    public void visit(OrderByElement orderBy) {
        super.visit(orderBy);
    }
}
