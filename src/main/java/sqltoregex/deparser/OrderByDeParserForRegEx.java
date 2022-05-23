package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OrderByDeParserForRegEx extends OrderByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    public static final String ORDER = "ORDER";
    public static final String SIBLINGS = "SIBLINGS";
    public static final String BY = "BY";
    public static final String DESC = "DESC";
    public static final String ASC = "ASC";
    public static final String NULLS_LAST = "NULLS LAST";
    public static final String NULLS_FIRST = "NULLS FIRST";
    private ExpressionVisitor regExExpressionVisitor;
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<String> columnNameSpellingMistake;
    private RegExGenerator<List<String>> columnNameOrder;

    public OrderByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionVisitor, buffer);
        this.regExExpressionVisitor = expressionVisitor;
        this.setColumnNameSpellingMistake(settingsManager);
        this.setKeywordSpellingMistake(settingsManager);
        this.setColumnNameOrder(settingsManager);
    }

    public void setKeywordSpellingMistake(SettingsManager settingsManager){
        this.keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
    }

    public String useKeywordSpellingMistake(String str){
        if(null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    public void setColumnNameSpellingMistake(SettingsManager settingsManager){
        this.columnNameSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMESPELLING, SpellingMistake.class);
    }

    public String useColumnNameSpellingMistake(String str){
        if(null != this.columnNameSpellingMistake) return this.columnNameSpellingMistake.generateRegExFor(str);
        else return str;
    }

    public void setColumnNameOrder(SettingsManager settingsManager){
        this.columnNameOrder = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
    }

    public String useColumnNameOrder(List<String> strlist){
        if(null != this.columnNameOrder) return this.columnNameOrder.generateRegExFor(strlist);
        else {
            StringBuilder str = new StringBuilder();
            Iterator<String> stringListIterator = strlist.iterator();
            while (stringListIterator.hasNext()){
                str.append(stringListIterator.next());
                if (stringListIterator.hasNext()) str.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            return str.toString();
        }
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
        buffer.append(useKeywordSpellingMistake(ORDER));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append("(?:");
        buffer.append(useKeywordSpellingMistake(SIBLINGS));
        buffer.append(REQUIRED_WHITE_SPACE + ")?");
        buffer.append(useKeywordSpellingMistake(BY));
        buffer.append(REQUIRED_WHITE_SPACE);

        List<String> orderByElementsAsStrings = new LinkedList<>();
        for(OrderByElement orderByElement : orderByElementList){
            orderByElementsAsStrings.add(deParseElementForOrderRotation(orderByElement));
        }
        buffer.append(useColumnNameOrder(orderByElementsAsStrings));
    }

    private String handleAscDesc(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();

        if (orderByElement.isAscDescPresent()) {
            temp.append(REQUIRED_WHITE_SPACE);
            temp.append(useKeywordSpellingMistake(ASC));
        } else if(!orderByElement.isAsc()) {
            temp.append(REQUIRED_WHITE_SPACE);
            temp.append(useKeywordSpellingMistake(DESC));
        }

        return temp.toString();
    }

    private String handleNullFirstLast(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();
        if (orderByElement.getNullOrdering() != null) {
            temp.append(REQUIRED_WHITE_SPACE);
            if(orderByElement.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST){
                temp.append(useKeywordSpellingMistake(NULLS_FIRST));
            } else {
                temp.append(useKeywordSpellingMistake(NULLS_LAST));
            }
        }
        return temp.toString();
    }

    public String deParseElementForOrderRotation(OrderByElement orderByElement){
        StringBuilder temp = new StringBuilder();
        temp.append(useColumnNameSpellingMistake(orderByElement.getExpression().toString()));
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
