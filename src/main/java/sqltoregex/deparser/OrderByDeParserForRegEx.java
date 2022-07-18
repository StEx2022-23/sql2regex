package sqltoregex.deparser;

import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static sqltoregex.deparser.StatementDeParserForRegEx.QUOTATION_MARK_REGEX;
import static sqltoregex.deparser.StatementDeParserForRegEx.REQUIRED_WHITE_SPACE;
import static sqltoregex.deparser.StatementDeParserForRegEx.OPTIONAL_WHITE_SPACE;

/**
 * Implements own {@link OrderByDeParser} to generate regex.
 */
public class OrderByDeParserForRegEx extends OrderByDeParser {
    private final StringSynonymGenerator aggregateFunctionLang;
    private final OrderRotation columnNameOrder;
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake keywordSpellingMistake;
    private final StringSynonymGenerator specialSynonyms;
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;

    /**
     * Short constructor for OrderByDeParserForRegEx. Inits the expanded constructor.
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(SettingsContainer settings) {
        this(null, new StringBuilder(), settings);
    }

    /**
     * Extended constructor for OrderByDeParserForRegEx.
     * @param expressionDeParser {@link ExpressionDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public OrderByDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                   SettingsContainer settings) {
        super(expressionDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.columnNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.specialSynonyms = settings.get(StringSynonymGenerator.class).get(SettingsOption.OTHERSYNONYMS);
        this.aggregateFunctionLang = settings.get(StringSynonymGenerator.class).get(SettingsOption.AGGREGATEFUNCTIONLANG);
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
                List<String> ascSynonyms = StringSynonymGenerator.generateAsListOrDefault(this.specialSynonyms, "ASC");
                handleAscDescSynonyms(temp, ascSynonyms);
            } else {
                List<String> descSynonyms = StringSynonymGenerator.generateAsListOrDefault(this.specialSynonyms, "DESC");
                handleAscDescSynonyms(temp, descSynonyms);
            }
        }

        return temp.toString();
    }

    /**
     * Checks if synonyms for asc or desc are given. Generates a regex.
     * @param temp {@link StringBuilder}
     * @param synonyms list with synonyms
     */
    private void handleAscDescSynonyms(StringBuilder temp, List<String> synonyms) {
        temp.append(REQUIRED_WHITE_SPACE);
        Iterator<String> stringIterator = synonyms.iterator();
        temp.append("(?:");
        while(stringIterator.hasNext()){
            temp.append(
                    StatementDeParserForRegEx.addQuotationMarks(
                            SpellingMistake.useOrDefault(
                                    this.keywordSpellingMistake,
                                    stringIterator.next()
                            )
                    )
            );
            if(stringIterator.hasNext()) temp.append("|");
        }
        temp.append(")");
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

    /**
     * Performs inserting of the table name alias. Extracted from a {@link FromItem}.
     * @param fromItem {@link FromItem}
     * @param col column name
     * @return generated regex
     */
    private String handleTableNameAlias(FromItem fromItem, String col) {
        StringBuilder temp = new StringBuilder();

        String table = fromItem.toString().split(" ")[0].replaceAll(QUOTATION_MARK_REGEX, "");
        String tableAlias = this.expressionDeParserForRegEx.getRelatedTableNameOrAlias(table);

        temp.append("(?:");
        temp.append(
                StatementDeParserForRegEx.addQuotationMarks(
                    SpellingMistake.useOrDefault(
                            this.columnNameSpellingMistake,
                            table
                    )
                )
        );
        temp.append("|");
        temp.append(
                StatementDeParserForRegEx.addQuotationMarks(
                        SpellingMistake.useOrDefault(
                                this.columnNameSpellingMistake,
                                tableAlias
                        )
                )
        );
        temp.append(")?\\.?");

        String columnName = col.split("\\.")[1].replaceAll(QUOTATION_MARK_REGEX, "");

        temp.append(
                StatementDeParserForRegEx.addQuotationMarks(
                        SpellingMistake.useOrDefault(
                                this.columnNameSpellingMistake,
                                columnName.replaceAll(QUOTATION_MARK_REGEX, "")
                        )
                )
        );
        return temp.toString();
    }
}
