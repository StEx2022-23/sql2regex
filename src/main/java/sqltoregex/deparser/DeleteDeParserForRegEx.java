package sqltoregex.deparser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import sqltoregex.settings.SettingsContainer;

import java.util.Iterator;

import static java.util.stream.Collectors.joining;

/**
 * implements own delete statement deparser for regular expressions
 */
public class DeleteDeParserForRegEx extends DeleteDeParser {
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SettingsContainer settingsContainer;

    /**
     * default constructor
     * @param settingsContainer SettingsContainer
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer) {
        this(settingsContainer, new ExpressionDeParserForRegEx(settingsContainer), new StringBuilder());
    }

    /**
     * explicit constructor with specific ExpressionDeParserForRegEx and StringBuilder
     * @param settingsContainer SettingsContainer
     * @param expressionDeParserForRegEx ExpressionDeParserForRegEx
     * @param buffer StringBuilder
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer, ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.settingsContainer = settingsContainer;
    }

    /**
     * overrides deparse method for implement regular expressions while deparsing the update statement
     * @param delete Delete
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(Delete delete) {
        if (delete.getWithItemsList() != null && !delete.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = delete.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                buffer.append(withItem);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        buffer.append("DELETE");
        if (delete.getModifierPriority() != null) {
            buffer.append(" ").append(delete.getModifierPriority());
        }
        if (delete.isModifierQuick()) {
            buffer.append(" QUICK");
        }
        if (delete.isModifierIgnore()) {
            buffer.append(" IGNORE");
        }
        if (delete.getTables() != null && !delete.getTables().isEmpty()) {
            buffer.append(
                    delete.getTables().stream().map(Table::getFullyQualifiedName).collect(joining(", ", " ", "")));
        }

        if (delete.getOutputClause()!=null) {
            delete.getOutputClause().appendTo(buffer);
        }

        if (delete.isHasFrom()) {
            buffer.append(" FROM");
        }
        buffer.append(" ").append(delete.getTable().toString());

        if (delete.getUsingList() != null && !delete.getUsingList().isEmpty()) {
            buffer.append(" USING").append(
                    delete.getUsingList().stream().map(Table::toString).collect(joining(", ", " ", "")));
        }
        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }

        if (delete.getWhere() != null) {
            buffer.append(" WHERE ");
            delete.getWhere().accept(this.getExpressionDeParserForRegEx());
        }

        if (delete.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(
                    this.getExpressionDeParserForRegEx(),
                    buffer,
                    this.getSettingsContainer()
            ).deParse(delete.getOrderByElements());
        }
        if (delete.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, settingsContainer).deParse(delete.getLimit());
        }

        if (delete.getReturningExpressionList() != null) {
            buffer.append(" RETURNING ").append(PlainSelect.
                    getStringList(delete.getReturningExpressionList(), true, false));
        }
    }

    /**
     * get ExpressionDeParserForRegEx
     * @return ExpressionDeParserForRegEx
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * set ExpressionDeParserForRegEx
     * @param expressionDeParserForRegEx ExpressionDeParserForRegEx
     */
    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * get private final settings container, init while construct the object
     * @return SettingsContainer
     */
    private SettingsContainer getSettingsContainer(){
        return this.settingsContainer;
    }

}
