package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.GroupByElementRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.ArrayList;
import java.util.List;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final GroupByElementRotation groupByElementOrder;
    private final ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SettingsContainer settings;

    public GroupByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.groupByElementOrder = settings.get(GroupByElementRotation.class).get(SettingsOption.GROUPBYELEMENTORDER);
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
        if (this.groupByElementOrder != null) {
            this.groupByElementOrder.setCapturingGroup(true);
        }

        buffer.append(RegExGenerator.useExpressionRotation(this.groupByElementOrder, this.expressionDeParserForRegEx,
                                                           expressions));
        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }

    public GroupByElementRotation getGroupByElementOrder(){
        return this.groupByElementOrder;
    }

    public List<String> expressionListToStringList(List<Expression> expressionList){
        List<String> deParsedExpressionsAsString = new ArrayList<>();
        StringBuilder tempBuffer = new StringBuilder();
        ExpressionDeParserForRegEx tempExpressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SelectDeParserForRegEx(this.settings), tempBuffer, this.settings);

        for(Expression expression : expressionList){
            expression.accept(tempExpressionDeParserForRegEx);
            deParsedExpressionsAsString.add(tempBuffer.toString());
            tempBuffer.replace(0, tempBuffer.length(), "");
        }

        return deParsedExpressionsAsString;
    }
}
