package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.ExpressionRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final boolean isKeywordSpellingMistake;
    private RegExGenerator<String> keywordSpellingMistake;
    private final RegExGenerator<List<Expression>> expressionRotation;
    ExpressionVisitor expressionVisitor;
    StringBuilder stringBuilder;

    public GroupByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionVisitor, buffer);
        this.expressionVisitor = expressionVisitor;
        this.stringBuilder = buffer;
        this.isKeywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING);
        if(this.isKeywordSpellingMistake){
            keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        }
        expressionRotation = settingsManager.getSettingBySettingOption(SettingsOption.EXPRESSIONORDER, ExpressionRotation.class);

    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("GROUP") : "GROUP");
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("BY") : "BY");
        buffer.append(REQUIRED_WHITE_SPACE);

        if (groupBy.isUsingBrackets()) {
            buffer.append("(");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();
        expressionRotation.setCapturingGroup(true);
        buffer.append(expressionRotation.generateRegExFor(expressions));

        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }
}
