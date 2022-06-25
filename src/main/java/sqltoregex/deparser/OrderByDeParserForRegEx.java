package sqltoregex.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
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
    private final OrderRotation columnNameOrder;
    private final SpellingMistake keywordSpellingMistake;
    private final StringSynonymGenerator specialSynonyms;
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SettingsContainer settings;
    /**
     * Short constructor for OrderByDeParserForRegEx. Inits the expanded constructor.
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(SettingsContainer settings) {
        this(null, new StringBuilder(), settings);
    }

    /**
     * Extended constructor for OrderByDeParserForRegEx.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.specialSynonyms = settings.get(StringSynonymGenerator.class).get(SettingsOption.OTHERSYNONYMS);
        this.settings = settings;
    }

    /**
     * Performs deparsing for {@link OrderByElement}. Needs an {@link FromItem} object to extract table name alias.
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
     * Prepares {@link OrderByElement} for {@link OrderRotation}. Needs an {@link FromItem} object to extract table name alias.
     * @param orderByElement {@link OrderByElement}
     * @param fromItem {@link FromItem}
     * @return generated regex
     */
    public String deParseElementForOrderRotation(OrderByElement orderByElement, FromItem fromItem) {
        StringBuilder temp = new StringBuilder();
        ExpressionDeParserForRegEx tempExpressionDeParserForRegEx = new ExpressionDeParserForRegEx(
                                                                        new SelectDeParserForRegEx(this.settings),
                                                                        temp,
                                                                        this.settings
                                                                    );
        if(fromItem != null) tempExpressionDeParserForRegEx.addTableNameAlias(fromItem.toString());
        orderByElement.getExpression().accept(tempExpressionDeParserForRegEx);
        temp.append(this.handleAscDesc(orderByElement));
        temp.append(this.handleNullFirstLast(orderByElement));
        return temp.toString();
    }

    /**
     * Gets the set {@link ExpressionVisitor}.
     * @return {@link ExpressionVisitor}
     */
    public ExpressionVisitor getExpressionVisitor() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * Sets a {@link ExpressionDeParserForRegEx}.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     */
    public void setExpressionVisitor(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * Gets the set {@link ExpressionDeParserForRegEx}.
     * @return {@link ExpressionDeParserForRegEx}
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * Sets {@link ExpressionDeParserForRegEx}.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     */
    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * Generates asc and desc expression from a {@link OrderByElement}.
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
     * Generates null first or last keyword expression.
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
}
