package sqltoregex.deparser;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import sqltoregex.settings.SettingsManager;

public class InsertDeParserForRegEx extends InsertDeParser {
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;

    public InsertDeParserForRegEx(SettingsManager settingsManager) {
        this(new ExpressionDeParserForRegEx(settingsManager), new SelectDeParserForRegEx(settingsManager), new StringBuilder(), settingsManager);
    }

    public InsertDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, selectDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
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
