package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private boolean isKeywordSpellingMistake;
    private RegExGenerator<List<String>> columnNameOrder;
    private RegExGenerator<String> keywordSpellingMistake;
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
        this.columnNameOrder = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
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
        System.out.println(expressions);

        buffer.append("(?:");
        for(int i = 0; i < expressions.size(); i++){
            buffer.append("(");
            Iterator<Expression> expressionIterator = expressions.listIterator();
            while (expressionIterator.hasNext()){
                buffer.append(expressionIterator.next().toString());
                if(expressionIterator.hasNext()){
                    buffer.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
                }
            }
            buffer.append(")");
            expressions.add(expressions.get(0));
            expressions.remove(0);
            if(i<expressions.size()-1) buffer.append("|");
        }
        buffer.append(")");

        if (groupBy.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }

        if (!groupBy.getGroupingSets().isEmpty()) {
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("GROUPING") : "GROUPING");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("SETS") : "SETS");
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append("(");

            boolean first = true;
            for (Object o : groupBy.getGroupingSets()) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                }
                if (o instanceof Expression) {
                    buffer.append(o);
                } else if (o instanceof ExpressionList list) {
                    buffer.append(list.getExpressions() == null ? "()" : list.toString());
                }
            }
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(")");
        }
    }
}
