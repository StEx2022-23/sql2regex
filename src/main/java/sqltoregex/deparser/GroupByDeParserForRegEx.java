package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements own {@link GroupByDeParser} to generate regex.
 */
public class GroupByDeParserForRegEx extends GroupByDeParser {
    Map<String, String> tableNameAliasMap = new HashMap<>();
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final OrderRotation groupByOrderRotation;
    private final ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SettingsContainer settings;

    /**
     * Constructor for GroupByDeParserForRegEx
     * @param expressionDeParser {@link ExpressionDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public GroupByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.groupByOrderRotation = settings.get(OrderRotation.class).get(SettingsOption.GROUPBYELEMENTORDER);
        this.settings = settings;
    }

    /**
     * Handle deparsing for {@link GroupByElement}.
     * {@link SuppressWarnings}: PMD.CyclomaticComplexity and PMD.NPathComplexity
     * @param groupBy {@link GroupByElement}
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "GROUP"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "BY"));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (groupBy.isUsingBrackets()) {
            buffer.append("(");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();

        buffer.append(OrderRotation.useOrDefault(this.groupByOrderRotation, this.expressionListToStringList(expressions)));
        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }

    /**
     * Get set {@link ExpressionDeParserForRegEx}.
     * @return {@link ExpressionDeParserForRegEx}
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx(){
        return this.expressionDeParserForRegEx;
    }

    /**
     * Set map with table names and related alias.
     * @param tableNameAliasMap map with key instanceof string and value instanceof string
     */
    public void setTableNameAliasMap(Map<String, String> tableNameAliasMap){
        this.tableNameAliasMap = tableNameAliasMap;
    }

    /**
     * Get table name alias map.
     * @return map with key instanceof string and value instanceof string
     */
    public Map<String, String> getTableNameAliasMap(){
        return this.tableNameAliasMap;
    }

    /**
     * Deparse an expression list to a string list.
     * @param expressionList list of {@link Expression}
     * @return list of string
     */
    public List<String> expressionListToStringList(List<Expression> expressionList){
        List<String> deParsedExpressionsAsString = new ArrayList<>();
        StringBuilder tempBuffer = new StringBuilder();
        ExpressionDeParserForRegEx tempExpressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SelectDeParserForRegEx(this.settings), tempBuffer, this.settings);
        tempExpressionDeParserForRegEx.setTableNameAliasMap(this.getTableNameAliasMap());
        for(Expression expression : expressionList){
            expression.accept(tempExpressionDeParserForRegEx);
            deParsedExpressionsAsString.add(tempBuffer.toString());
            tempBuffer.replace(0, tempBuffer.length(), "");
        }
        return deParsedExpressionsAsString;
    }
}
