package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    Map<String, String> tableNameAliasMap = new HashMap<>();
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final OrderRotation groupByOrderRotation;
    private final ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SettingsContainer settings;

    public GroupByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.groupByOrderRotation = settings.get(OrderRotation.class).get(SettingsOption.GROUPBYELEMENTORDER);
        this.settings = settings;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "GROUP"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "BY"));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (groupBy.isUsingBrackets()) {
            buffer.append("(");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();

        buffer.append(RegExGenerator.useOrderRotation(this.groupByOrderRotation, this.expressionListToStringList(expressions)));
        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }

    public ExpressionDeParserForRegEx expressionDeParserForRegEx(){
        return this.expressionDeParserForRegEx;
    }

    public void setTableNameAliasMap(Map<String, String> tableNameAliasMap){
        this.tableNameAliasMap = tableNameAliasMap;
    }

    public Map<String, String> getTableNameAliasMap(){
        return this.tableNameAliasMap;
    }

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
