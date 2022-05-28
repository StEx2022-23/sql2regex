package sqltoregex.deparser;

import ch.qos.logback.classic.db.names.TableName;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.util.LinkedList;
import java.util.List;

public class OrderByDeParserForRegEx extends OrderByDeParser {
    public static final String ORDER = "ORDER";
    public static final String SIBLINGS = "SIBLINGS";
    public static final String BY = "BY";
    public static final String DESC = "DESC";
    public static final String ASC = "ASC";
    public static final String NULLS_LAST = "NULLS LAST";
    public static final String NULLS_FIRST = "NULLS FIRST";
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private final OrderRotation columnNameOrder;
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake keywordSpellingMistake;
    private final SynonymGenerator<?, String> specialSynonyms;
    private ExpressionVisitor regExExpressionVisitor;
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<String> columnNameSpellingMistake;
    private RegExGenerator<List<String>> columnNameOrder;
    private RegExGenerator<String> specialSynonyms;
    private RegExGenerator<String> aggregateFunctionLang;

    public OrderByDeParserForRegEx(SettingsManager settingsManager) {
        this(new ExpressionVisitorAdapter(), new StringBuilder(), settingsManager);
    }

    public OrderByDeParserForRegEx(ExpressionVisitor expressionVisitor, StringBuilder buffer,
                                   SettingsManager settingsManager) {
        super(expressionVisitor, buffer);
        this.regExExpressionVisitor = expressionVisitor;
        this.columnNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMESPELLING,
                                                                                    SpellingMistake.class).orElse(null);
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                                                                                 SpellingMistake.class).orElse(null);
        this.columnNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER,
                                                                          OrderRotation.class).orElse(null);
        this.specialSynonyms = settingsManager.getSettingBySettingsOption(SettingsOption.OTHERSYNONYMS,
                                                                          StringSynonymGenerator.class).orElse(null);
    }

    public void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList, FromItem fromItem) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ORDER));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append("(?:");
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, SIBLINGS));
        buffer.append(REQUIRED_WHITE_SPACE + ")?");
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, BY));
        buffer.append(REQUIRED_WHITE_SPACE);

        List<String> orderByElementsAsStrings = new LinkedList<>();
        for (OrderByElement orderByElement : orderByElementList) {
            orderByElementsAsStrings.add(deParseElementForOrderRotation(orderByElement));
        }
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, orderByElementsAsStrings));
    }

    @Override
    public void deParseElement(OrderByElement orderBy) {
        throw new UnsupportedOperationException();
    }

    public String deParseElementForOrderRotation(OrderByElement orderByElement, FromItem fromItem){
        StringBuilder temp = new StringBuilder();
        if(orderByElement.getExpression().toString().contains("(") && orderByElement.getExpression().toString().contains(")")){
            temp.append(useAggregateFunctionLang(orderByElement.getExpression().toString().replaceAll("\\(.*", "")));
            temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);

            String tempColumn = orderByElement.getExpression().toString().split("\\(")[1].split("\\)")[0];
            if(tempColumn.contains(".")){
                temp.append(this.handleTableNameAlias(fromItem, tempColumn));
            } else {
                temp.append(useColumnNameSpellingMistake(tempColumn));
            }
            temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
        } else{
            if(orderByElement.getExpression().toString().contains(".")){
                temp.append(this.handleTableNameAlias(fromItem, orderByElement.getExpression().toString()));
            } else {
                temp.append(useColumnNameSpellingMistake(orderByElement.getExpression().toString()));
            }
        }
        temp.append(this.handleAscDesc(orderByElement));
        temp.append(this.handleNullFirstLast(orderByElement));
        temp.append(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE);
        return temp.toString();
    }

    public ExpressionVisitor getExpressionVisitor() {
        return this.regExExpressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
        this.regExExpressionVisitor = expressionVisitor;
    }

    private String handleAscDesc(OrderByElement orderByElement) {
        StringBuilder temp = new StringBuilder();

        if (orderByElement.isAscDescPresent()) {
            if (orderByElement.isAsc()) {
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append("(?:").append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ASC))
                        .append("|").append(RegExGenerator.useStringSynonymGenerator(this.specialSynonyms, ASC))
                        .append(")");
            } else {
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append("(?:").append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, DESC))
                        .append("|").append(RegExGenerator.useStringSynonymGenerator(this.specialSynonyms, DESC))
                        .append(")");
            }
        }

        return temp.toString();
    }

    private String handleNullFirstLast(OrderByElement orderByElement) {
        StringBuilder temp = new StringBuilder();
        if (orderByElement.getNullOrdering() != null) {
            temp.append(REQUIRED_WHITE_SPACE);
            if (orderByElement.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST) {
                temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NULLS_FIRST));
            } else {
                temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NULLS_LAST));
            }
        }

    private String handleTableNameAlias(FromItem fromItem, String tempColumn){
        StringBuilder temp = new StringBuilder();
        String columnName = tempColumn.split("\\.")[1];
        temp.append("(?:");
        temp.append(fromItem.toString().split(" ")[0]);
        temp.append("|");
        temp.append(fromItem.getAlias().toString().replace(" ", ""));
        temp.append(")?\\.?");
        temp.append(useColumnNameSpellingMistake(columnName));
        return temp.toString();
    }
}
