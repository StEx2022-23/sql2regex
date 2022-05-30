package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class UpdateDeParserForRegEx extends UpdateDeParser {
    private final SpellingMistake keywordSpellingMistake;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;

    public UpdateDeParserForRegEx(SettingsManager settingsManager, StringBuilder buffer) {
        this(new ExpressionDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public UpdateDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                SpellingMistake.class).orElse(null);
    }

    @Override
    public void deParse(Update update) {
        super.deParse(update);
    }

    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return super.getExpressionVisitor();
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        super.setExpressionVisitor(visitor);
    }

    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    @Override
    public void visit(OrderByElement orderBy) {
        super.visit(orderBy);
    }
}
