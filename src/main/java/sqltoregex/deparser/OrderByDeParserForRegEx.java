package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.LinkedList;
import java.util.List;

public class OrderByDeParserForRegEx extends OrderByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    public static final String ORDER = "ORDER";
    public static final String SIBLINGS = "SIBLINGS";
    public static final String BY = "BY";
    public static final String DESC = "DESC";
    public static final String ASC = "ASC";
    public static final String NULLS_LAST = "NULLS LAST";
    public static final String NULLS_FIRST = "NULLS FIRST";
    private ExpressionVisitor regExExpressionVisitor;
    private final boolean isKeywordSpellingMistake;
    private RegExGenerator<String> keywordSpellingMistake;
    private final boolean isColumnNameSpellingMistake;
    private RegExGenerator<String> columnNameSpellingMistake;
    private final RegExGenerator<List<String>> orderRotation;

    public OrderByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionVisitor, buffer);
        this.regExExpressionVisitor = expressionVisitor;
        this.isColumnNameSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMESPELLING);
        if(this.isColumnNameSpellingMistake){
            columnNameSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        }
        this.isKeywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING);
        if(this.isKeywordSpellingMistake){
            keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        }
        orderRotation = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
    }

    public void setExpressionVisitor(ExpressionVisitor expressionVisitor){
        this.regExExpressionVisitor = expressionVisitor;
    }

    public ExpressionVisitor getExpressionVisitor(){
        return this.regExExpressionVisitor;
    }

    @Override
    public void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(isKeywordSpellingMistake ? this.keywordSpellingMistake.generateRegExFor(ORDER) : ORDER);
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append("(?:");
        buffer.append(isKeywordSpellingMistake ? this.keywordSpellingMistake.generateRegExFor(SIBLINGS) : SIBLINGS);
        buffer.append(REQUIRED_WHITE_SPACE + ")?");
        buffer.append(isKeywordSpellingMistake ? this.keywordSpellingMistake.generateRegExFor(BY) : BY);
        buffer.append(REQUIRED_WHITE_SPACE);

        List<String> orderByElementsAsStrings = new LinkedList<>();
        for(OrderByElement orderByElement : orderByElementList){
            orderByElementsAsStrings.add(deParseElementForOrderRotation(orderByElement));
        }
        buffer.append(orderRotation.generateRegExFor(orderByElementsAsStrings));
    }

    private String handleAscDesc(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();
        if (!orderByElement.isAsc()) {
            temp.append(REQUIRED_WHITE_SPACE);
            temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(DESC) : DESC);
        } else if (orderByElement.isAscDescPresent()) {
            temp.append(REQUIRED_WHITE_SPACE);
            temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(ASC) : ASC);
        }
        return temp.toString();
    }

    private String handleNullFirstLast(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();
        if (orderByElement.getNullOrdering() != null) {
            temp.append(REQUIRED_WHITE_SPACE);
            if(orderByElement.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST){
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(NULLS_FIRST) : NULLS_FIRST);
            } else {
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(NULLS_LAST) : NULLS_LAST);
            }
        }
        return temp.toString();
    }

    public String deParseElementForOrderRotation(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();
        temp.append(isColumnNameSpellingMistake ? columnNameSpellingMistake.generateRegExFor(orderByElement.getExpression().toString()) : orderByElement.getExpression().toString());
        temp.append(this.handleAscDesc(orderByElement));
        temp.append(this.handleNullFirstLast(orderByElement));
        temp.append(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE);
        return temp.toString();
    }

    @Override
    public void deParseElement(OrderByElement orderBy) {
        throw new UnsupportedOperationException();
    }
}
