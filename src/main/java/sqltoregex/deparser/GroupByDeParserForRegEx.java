package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.util.deparser.GroupByDeParser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

public class GroupByDeParserForRegEx extends GroupByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private Integer groupByOrderOptionsCounter = 0;
    private final Map<Integer, List<Expression>> groupByOrderOptionsMap = new HashMap<>();
    private final boolean isKeywordSpellingMistake;
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
        generateGroupByOrderOptions(expressions);

        this.generateGroupByOrderOptions(expressions);
        buffer.append("(?:");
        for(Map.Entry<Integer, List<Expression>> entry : groupByOrderOptionsMap.entrySet()){
            buffer.append("(");
            Iterator<Expression> expressionIterator = groupByOrderOptionsMap.get(entry.getKey()).iterator();
            while(expressionIterator.hasNext()){
                expressionIterator.next().accept(expressionVisitor);
                if(expressionIterator.hasNext()){
                    buffer.append(",");
                }
            }
            buffer.append(")");
            buffer.append("|");
        }
        buffer.deleteCharAt(buffer.length()-1);
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

    private void generateGroupByOrderOptionsRek(Integer amount, List<Expression> valueList){
        if (amount == 1) {
            List<Expression> singleValue = new ArrayList<>(valueList);
            groupByOrderOptionsMap.put(groupByOrderOptionsCounter, singleValue);
            groupByOrderOptionsCounter++;
        } else {
            generateGroupByOrderOptionsRek(amount-1, valueList);
            for(int i = 0; i < amount-1;i++){
                if (amount % 2 == 0){
                    Expression temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(i));
                    valueList.set(i, temp);
                } else {
                    Expression temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(0));
                    valueList.set(0, temp);
                }
                generateGroupByOrderOptionsRek(amount-1, valueList);
            }
        }
    }

    public void generateGroupByOrderOptions(List<Expression> valueList){
        generateGroupByOrderOptionsRek(valueList.size(), valueList);
    }
}
