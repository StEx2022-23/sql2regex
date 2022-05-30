package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.Iterator;

public class UpdateDeParserForRegEx extends UpdateDeParser {
    private final SpellingMistake keywordSpellingMistake;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SettingsManager settingsManager;

    public UpdateDeParserForRegEx(SettingsManager settingsManager, StringBuilder buffer) {
        this(new ExpressionDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public UpdateDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, buffer);
        this.settingsManager = settingsManager;
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                SpellingMistake.class).orElse(null);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    public void deParse(Update update) {
        if (update.getWithItemsList() != null && !update.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = update.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                buffer.append(withItem);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        buffer.append("UPDATE ");
        if (update.getModifierPriority() != null) {
            buffer.append(update.getModifierPriority()).append(" ");
        }
        if (update.isModifierIgnore()) {
            buffer.append("IGNORE ");
        }
        buffer.append(update.getTable());
        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }
        buffer.append(" SET ");

        int j=0;
        for (UpdateSet updateSet : update.getUpdateSets()) {
            if (j > 0) {
                buffer.append(", ");
            }

            if (updateSet.isUsingBracketsForColumns()) {
                buffer.append("(");
            }
            for (int i = 0; i < updateSet.getColumns().size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                updateSet.getColumns().get(i).accept(this.getExpressionDeParserForRegEx());
            }
            if (updateSet.isUsingBracketsForColumns()) {
                buffer.append(")");
            }

            buffer.append(" = ");

            if (updateSet.isUsingBracketsForValues()) {
                buffer.append("(");
            }
            for (int i = 0; i < updateSet.getExpressions().size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                updateSet.getExpressions().get(i).accept(this.getExpressionDeParserForRegEx());
            }
            if (updateSet.isUsingBracketsForValues()) {
                buffer.append(")");
            }

            j++;
        }

        if (update.getOutputClause()!=null) {
            update.getOutputClause().appendTo(buffer);
        }

        if (update.getFromItem() != null) {
            buffer.append(" FROM ").append(update.getFromItem());
            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    if (join.isSimple()) {
                        buffer.append(", ").append(join);
                    } else {
                        buffer.append(" ").append(join);
                    }
                }
            }
        }

        if (update.getWhere() != null) {
            buffer.append(" WHERE ");
            update.getWhere().accept(this.getExpressionDeParserForRegEx());
        }
        if (update.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(this.getExpressionDeParserForRegEx(), buffer, this.settingsManager).deParse(update.getOrderByElements(), update.getFromItem());
        }
        if (update.getLimit() != null) {
            new LimitDeparser(buffer).deParse(update.getLimit());
        }

        if (update.getReturningExpressionList() != null) {
            buffer.append(" RETURNING ").append(PlainSelect.
                    getStringList(update.getReturningExpressionList(), true, false));
        }
    }

    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    @Override
    public void visit(OrderByElement orderBy) {
        throw new UnsupportedOperationException();
    }
}
