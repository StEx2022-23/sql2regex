package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.regexgenerator.*;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;

import java.util.*;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    public static final String GROUP = "GROUP";
    public static final String BY = "BY";
    private final SpellingMistake keywordSpellingMistake;
    private final ExpressionRotation expressionOrder;
    private final ExpressionDeParserForRegEx expressionDeParserForRegEx;

    public GroupByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class).orElse(null);
        this.expressionOrder = settingsManager.getSettingBySettingsOption(SettingsOption.EXPRESSIONORDER, ExpressionRotation.class).orElse(null);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, GROUP));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, BY));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (groupBy.isUsingBrackets()) {
            buffer.append("(");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();
        if (this.expressionOrder != null){
            this.expressionOrder.setCapturingGroup(true);
        }
        buffer.append(RegExGenerator.useExpressionRotation(this.expressionOrder, this.expressionDeParserForRegEx, expressions, buffer));

        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }
}
