package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements own {@link OrderByDeParser} to generate regex.
 */
public class OrderByDeParserForRegEx extends OrderByDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final StringSynonymGenerator aggregateFunctionLang;
    private final OrderRotation columnNameOrder;
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake keywordSpellingMistake;
    private final StringSynonymGenerator specialSynonyms;
    private ExpressionVisitor expressionDeParserForRegEx;

    /**
     * Short constructor for OrderByDeParserForRegEx. Init the expanded constructor.
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(SettingsContainer settings) {
        this(new ExpressionVisitorAdapter(), new StringBuilder(), settings);
    }

    /**
     * Extended constructor for OrderByDeParserForRegEx.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(ExpressionVisitor expressionDeParserForRegEx, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.columnNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.specialSynonyms = settings.get(StringSynonymGenerator.class).get(SettingsOption.OTHERSYNONYMS);
        this.aggregateFunctionLang = settings.get(StringSynonymGenerator.class).get(SettingsOption.AGGREGATEFUNCTIONLANG);
    }

    /**
     * Handle deparsing for {@link OrderByElement}. Need a {@link FromItem} object to handle table name alias.
     * @param orderByElementList list of {@link OrderByElement}
     * @param fromItem {@link FromItem}
     */
    public void deParse(List<OrderByElement> orderByElementList, FromItem fromItem) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ORDER"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append("(?:");
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "SIBLINGS"));
        buffer.append(REQUIRED_WHITE_SPACE + ")?");
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "BY"));
        buffer.append(REQUIRED_WHITE_SPACE);

        List<String> orderByElementsAsStrings = new LinkedList<>();
        for (OrderByElement orderByElement : orderByElementList) {
            orderByElementsAsStrings.add(deParseElementForOrderRotation(orderByElement, fromItem));
        }
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, orderByElementsAsStrings));
    }

    /**
     * Prepare {@link OrderByElement} for {@link OrderRotation}. Need a {@link FromItem} object to handle table name alias.
     * @param orderByElement {@link OrderByElement}
     * @param fromItem {@link FromItem}
     * @return generated regex
     */
    public String deParseElementForOrderRotation(OrderByElement orderByElement, FromItem fromItem) {
        StringBuilder temp = new StringBuilder();
        if (orderByElement.getExpression().toString().contains("(") && orderByElement.getExpression().toString()
                .contains(")")) {
            temp.append(StringSynonymGenerator.useOrDefault(this.aggregateFunctionLang,
                                                                 orderByElement.getExpression().toString()
                                                                         .replaceAll("\\(.*", "")));
            temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);

            String tempColumn = orderByElement.getExpression().toString().split("\\(")[1].split("\\)")[0];
            if (tempColumn.contains(".")) {
                temp.append(this.handleTableNameAlias(fromItem, tempColumn));
            } else {
                temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, tempColumn));
            }
            temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
        } else {
            if (orderByElement.getExpression().toString().contains(".")) {
                temp.append(this.handleTableNameAlias(fromItem, orderByElement.getExpression().toString()));
            } else {
                temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake,
                                                              orderByElement.getExpression().toString()));
            }
        }
        temp.append(this.handleAscDesc(orderByElement));
        temp.append(this.handleNullFirstLast(orderByElement));
        return temp.toString();
    }

    /**
     * Get set {@link ExpressionVisitor}.
     * @return {@link ExpressionVisitor}
     */
    public ExpressionVisitor getExpressionVisitor() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * Set {@link ExpressionVisitor}.
     * @param expressionVisitor {@link ExpressionVisitor}
     */
    public void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionDeParserForRegEx = expressionVisitor;
    }

    /**
     * Get set {@link ExpressionDeParserForRegEx}.
     * @return {@link ExpressionDeParserForRegEx}
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return (ExpressionDeParserForRegEx) this.expressionDeParserForRegEx;
    }

    /**
     * Set {@link ExpressionDeParserForRegEx}.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     */
    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * Handle asc and desc deparsing from a {@link OrderByElement}.
     * @param orderByElement {@link OrderByElement}
     * @return generated regex
     */
    private String handleAscDesc(OrderByElement orderByElement) {
        StringBuilder temp = new StringBuilder();

        if (orderByElement.isAscDescPresent()) {
            if (orderByElement.isAsc()) {
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append("(?:").append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ASC"))
                        .append("|").append(StringSynonymGenerator.useOrDefault(this.specialSynonyms, "ASC"))
                        .append(")");
            } else {
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append("(?:").append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "DESC"))
                        .append("|").append(StringSynonymGenerator.useOrDefault(this.specialSynonyms, "DESC"))
                        .append(")");
            }
        }

        return temp.toString();
    }

    /**
     * Handle null first or last keyword.
     * @param orderByElement {@link OrderByElement}
     * @return generated regex
     */
    private String handleNullFirstLast(OrderByElement orderByElement) {
        StringBuilder temp = new StringBuilder();
        if (orderByElement.getNullOrdering() != null) {
            temp.append(REQUIRED_WHITE_SPACE);
            if (orderByElement.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST) {
                temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "NULLS"));
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "FIRST"));
            } else {
                temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "NULLS"));
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "LAST"));
            }
        }
        return temp.toString();
    }

    /**
     * Handle table name alias. Extracted from a {@link FromItem}.
     * @param fromItem {@link FromItem}
     * @param col column name
     * @return generated regex
     */
    private String handleTableNameAlias(FromItem fromItem, String col) {
        StringBuilder temp = new StringBuilder();
        String columnName = col.split("\\.")[1];
        temp.append("(?:");
        temp.append(fromItem.toString().split(" ")[0]);
        temp.append("|");
        temp.append(fromItem.getAlias().toString().replace(" ", ""));
        temp.append(")?\\.?");
        temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, columnName));
        return temp.toString();
    }
}
