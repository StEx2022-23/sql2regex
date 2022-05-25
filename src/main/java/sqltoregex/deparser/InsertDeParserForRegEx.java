package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.InsertDeParser;

public class InsertDeParserForRegEx extends InsertDeParser {
    public InsertDeParserForRegEx() {
        super();
    }

    public InsertDeParserForRegEx(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
        super(expressionVisitor, selectVisitor, buffer);
    }

    @Override
    public void deParse(Insert insert) {
        super.deParse(insert);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        super.visit(expressionList);
    }

    @Override
    public void visit(NamedExpressionList NamedExpressionList) {
        super.visit(NamedExpressionList);
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        super.visit(multiExprList);
    }

    @Override
    public void visit(SubSelect subSelect) {
        super.visit(subSelect);
    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return super.getExpressionVisitor();
    }

    @Override
    public SelectVisitor getSelectVisitor() {
        return super.getSelectVisitor();
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        super.setExpressionVisitor(visitor);
    }

    @Override
    public void setSelectVisitor(SelectVisitor visitor) {
        super.setSelectVisitor(visitor);
    }
}
