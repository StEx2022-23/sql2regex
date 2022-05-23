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
    public static final String GROUP = "GROUP";
    public static final String BY = "BY";
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<List<Expression>> expressionOrder;
    private final ExpressionVisitor expressionVisitor;

    public GroupByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionVisitor, buffer);
        this.expressionVisitor = expressionVisitor;
        this.setKeywordSpellingMistake(settingsManager);
        this.setExpressionOrder(settingsManager);
    }

    private void setKeywordSpellingMistake(SettingsManager settingsManager){
        this.keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
    }

    private String useKeywordSpellingMistake(String str){
        if(null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setExpressionOrder(SettingsManager settingsManager){
        this.expressionOrder = settingsManager.getSettingBySettingOption(SettingsOption.EXPRESSIONORDER, ExpressionRotation.class);
    }

    private String useExpressionOrder(List<Expression> expressionList, StringBuilder buffer){
        if(null != this.expressionOrder) return this.expressionOrder.generateRegExFor(expressionList);
        else {
            Iterator<Expression> expressionIterator = expressionList.iterator();
            while (expressionIterator.hasNext()){
                expressionIterator.next().accept(expressionVisitor);
                if(expressionIterator.hasNext()) buffer.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            return "";
        }
    }


    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(GroupByElement groupBy) {
        buffer.append(useKeywordSpellingMistake(GROUP));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake(BY));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (groupBy.isUsingBrackets()) {
            buffer.append("(");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();
        expressionOrder.setCapturingGroup(true);
        buffer.append(useExpressionOrder(expressions, buffer));

        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }
}
