package sqltoregex.deparser;

import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import sqltoregex.settings.SettingsContainer;

/**
 * implements own delete statement deparser for regular expressions
 */
public class DeleteDeParserForRegEx extends DeleteDeParser {
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;

    /**
     * default constructor
     * @param settingsContainer
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer) {
        this(settingsContainer, new ExpressionDeParserForRegEx(settingsContainer), new StringBuilder());
    }

    /**
     * explicit constructor with specific ExpressionDeParserForRegEx and StringBuilder
     * @param settingsContainer
     * @param expressionDeParserForRegEx
     * @param buffer
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer, ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * overrides deparse method for implement regular expressions while deparsing the update statement
     * @param delete
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(Delete delete) {
        super.deParse(delete);
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
     * @param expressionDeParserForRegEx
     */
    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

}
