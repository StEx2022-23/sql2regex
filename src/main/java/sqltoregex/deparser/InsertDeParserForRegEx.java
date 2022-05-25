package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import sqltoregex.settings.SettingsManager;

public class InsertDeParserForRegEx extends InsertDeParser {
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;

    public InsertDeParserForRegEx(SettingsManager settingsManager) {
        super();
        this.expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
    }

    public InsertDeParserForRegEx(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionVisitor, selectVisitor, buffer);
        this.expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settingsManager);
        this.selectDeParserForRegEx = new SelectDeParserForRegEx(settingsManager);
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
}
