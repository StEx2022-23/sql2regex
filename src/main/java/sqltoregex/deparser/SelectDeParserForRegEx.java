package sqltoregex.deparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.ValuesStatementDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.*;

import static sqltoregex.deparser.StatementDeParserForRegEx.*;

/**
 * Implements own {@link SelectDeParser} to generate regex.
 */
public class SelectDeParserForRegEx extends SelectDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final OrderRotation columnNameOrder;
    private final OrderRotation tableNameOrder;
    private final StringSynonymGenerator aggregateFunctionLang;
    private final SettingsContainer settingsContainer;
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;

    /**
     * Constructor for SelectDeParserForRegEx. Needs a {@link SettingsContainer}.
     * @param settingsContainer holds all actual settings
     */
    public SelectDeParserForRegEx(SettingsContainer settingsContainer) {
        super();
        this.settingsContainer = settingsContainer;
        this.expressionDeParserForRegEx = new ExpressionDeParserForRegEx(this, buffer, settingsContainer);
        this.keywordSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.aggregateFunctionLang = settingsContainer.get(StringSynonymGenerator.class).get(SettingsOption.AGGREGATEFUNCTIONLANG);
        this.columnNameOrder = settingsContainer.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.tableNameOrder = settingsContainer.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER);
        this.tableNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
    }

    /**
     * Generates an optional alias regex block.
     * @param isOptional boolean for optional block
     * @return generated regex
     */
    public String addOptionalAliasKeywords(boolean isOptional) {
        StringBuilder temp = new StringBuilder();
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("(?:");
        temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ALIAS"));
        temp.append("|");
        temp.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "AS"));
        if (isOptional) temp.append(")?");
        else temp.append(")");
        temp.append(REQUIRED_WHITE_SPACE);
        return temp.toString();
    }

    /**
     * Performs join deparsing.
     * @param join {@link Join}
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deparseJoin(Join join) {
        this.expressionDeParserForRegEx.addTableNameAlias(join.getRightItem().toString());
        if (join.isSimple() && join.isOuter()) {
            buffer.append(",");
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OUTER", true);
        } else if (join.isSimple()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
        } else {
            if (join.isRight()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "RIGHT", false);
            } else if (join.isNatural()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "NATURAL", false);
            } else if (join.isFull()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FULL", false);
            } else if (join.isLeft()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "LEFT", false);
            } else if (join.isCross()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "CROSS", false);
            }

            if (join.isOuter()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OUTER", false);
            } else if (join.isInner()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "INNER", false);
            } else if (join.isSemi()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "SEMI", false);
            }

            if (join.isStraight()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "STRAIGHT_JOIN", true);
            } else if (join.isApply()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "APPLY", true);
            } else {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "JOIN", true);
            }
        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "WITHIN", true);
            buffer.append(QUOTATION_MARK_REGEX_ZERO_ONE).append(join.getJoinWindow().toString()).append(QUOTATION_MARK_REGEX_ZERO_ONE);
        }
        for (Expression onExpression : join.getOnExpressions()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "ON", true);
            onExpression.accept(expressionDeParserForRegEx);
        }
        if (!join.getUsingColumns().isEmpty()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "USING", true);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE);
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext(); ) {
                Column column = iterator.next();
                buffer.append(QUOTATION_MARK_REGEX_ZERO_ONE).append(column.toString().replaceAll(QUOTATION_MARK_REGEX, "")).append(QUOTATION_MARK_REGEX_ZERO_ONE);
                if (iterator.hasNext()) {
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                }
            }
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        }
    }

    /**
     * Performs offset deparsing.
     * @param offset {@link Offset}
     */
    @Override
    public void deparseOffset(Offset offset) {
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OFFSET", true);
        buffer.append(offset.getOffset());
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (offset.getOffsetParam() != null) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, offset.getOffsetParam()));
        }
    }

    /**
     * Performs optimize for deparsing.
     * @param optimizeFor {@link OptimizeFor}
     */
    private void deparseOptimizeForForRegEx(OptimizeFor optimizeFor) {
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OPTIMIZE", true);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "FOR", true);
        buffer.append(optimizeFor.getRowCount());
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "ROWS", false);
    }

    /**
     * Returns the ExpressionVisitor. The ExpressionVisitor is instanceof {@link ExpressionDeParserForRegEx}
     * @return ExpressionVisitor instanceof {@link ExpressionDeParserForRegEx}
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * Sets the ExpressionVisitor. The ExpressionVisitor must be instanceof {@link ExpressionDeParserForRegEx}
     * @param visitor ExpressionVisitor instanceof {@link ExpressionDeParserForRegEx}
     * @throws IllegalArgumentException if the ExpressionVisitor isn't instanceof {@link ExpressionDeParserForRegEx}
     */
    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        if (!(visitor instanceof ExpressionDeParserForRegEx)) {
            throw new IllegalArgumentException(
                    "ExpressionVisitor must be of type ExpressionDeParserForRegex for this implementation");
        }
        this.expressionDeParserForRegEx = (ExpressionDeParserForRegEx) visitor;
    }

    /**
     * Generates regex for alias and aggregate functions.
     * @param o Object instanceof {@link net.sf.jsqlparser.statement.Statement}
     * @return generated regex
     */
    private String handleAliasAndAggregateFunction(Object o) {
        StringBuilder temp = new StringBuilder();
        if (o.toString().contains("(") && o.toString().contains(")")) {
            temp.append(StringSynonymGenerator.useOrDefault(this.aggregateFunctionLang,
                                                                 o.toString().replaceAll(QUOTATION_MARK_REGEX, "").replaceAll("\\(.*", "")));
            temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            temp.append(SpellingMistake.useOrDefault(
                    this.columnNameSpellingMistake,
                    o.toString().replaceAll(QUOTATION_MARK_REGEX, "").split("\\(")[1].split("\\)")[0])
            );
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
        }

        if (!o.toString().contains("AS") && !o.toString().contains("(") && !o.toString().contains(")")) {
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, o.toString().replaceAll(QUOTATION_MARK_REGEX, "")));
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
        }

        if (o.toString().contains("AS") && o.toString().contains("(") && o.toString().contains(")")) {
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            temp.append(o.toString().replaceAll(QUOTATION_MARK_REGEX, "").split("AS")[1].replace(" ", ""));
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
        }

        if (o.toString().contains("AS") && !o.toString().contains("(") && !o.toString().contains(")")) {
            temp.append(o.toString().replaceAll(QUOTATION_MARK_REGEX, "").split("AS")[0].replace(" ", ""));
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            temp.append(o.toString().replaceAll(QUOTATION_MARK_REGEX, "").split("AS")[1].replace(" ", ""));
            temp.append(QUOTATION_MARK_REGEX_ZERO_ONE);
        }

        if (!o.toString().contains("AS")) {
            temp.append(OPTIONAL_WHITE_SPACE);
            temp.append("(");
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(".*)?");
        }
        return temp.toString();
    }

    /**
     * Performs {@link WithItem} deparsing.
     * @param withItem {@link WithItem}
     * @return generated regex
     */
    private String handleWithGetItemListIsUsingValue(WithItem withItem) {
        StringBuilder temp = new StringBuilder();
        ItemsList itemsList = withItem.getItemsList();
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "VALUE", true);
        temp.append("S?");
        temp.append("\\(").append(OPTIONAL_WHITE_SPACE);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "VALUE", true);
        temp.append("S?");
        ExpressionList expressionList = (ExpressionList) itemsList;
        Iterator<Expression> expressionIterator = expressionList.getExpressions().iterator();
        List<String> expressionListAsStrings = new LinkedList<>();
        while (expressionIterator.hasNext()) {
            Expression selectItem = expressionIterator.next();
            expressionListAsStrings.add(selectItem.toString());
        }
        temp.append(OrderRotation.useOrDefault(this.columnNameOrder, expressionListAsStrings));
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("\\)");
        return temp.toString();
    }

    /**
     * Extracts value list of {@link WithItem} from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param select {@link Select}
     * @return generated regex for value list
     */
    public String handleWithItemValueList(Select select) {
        return OrderRotation.useOrDefault(this.tableNameOrder,
                                               helperFunctionForHandleWithItemValueList(select.getWithItemsList()));
    }

    /**
     * Extracts value list of {@link WithItem} from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param subSelect {@link SubSelect}
     * @return generated regex for value list
     */
    public String handleWithItemValueList(SubSelect subSelect) {
        return OrderRotation.useOrDefault(this.tableNameOrder,
                                               helperFunctionForHandleWithItemValueList(subSelect.getWithItemsList()));
    }

    /**
     * Extracts value list of {@link WithItem} from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param update {@link Update}
     * @return generated regex for value list
     */
    public String handleWithItemValueList(Update update) {
        return OrderRotation.useOrDefault(this.tableNameOrder,
                helperFunctionForHandleWithItemValueList(update.getWithItemsList()));
    }

    /**
     * Extracts value list of {@link WithItem} from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param delete {@link Delete}
     * @return generated regex for value list
     */
    public String handleWithItemValueList(Delete delete) {
        return OrderRotation.useOrDefault(this.tableNameOrder,
                helperFunctionForHandleWithItemValueList(delete.getWithItemsList()));
    }

    /**
     * Extracts {@link WithItem} list from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param listOfWithItems list of {@link WithItem}
     * @return list of string with deparsed items
     */
    private List<String> helperFunctionForHandleWithItemValueList(List<WithItem> listOfWithItems) {
        List<String> withItemStringList = new LinkedList<>();
        for (WithItem withItem : listOfWithItems) {
            StringBuilder temp = new StringBuilder();
            if (withItem.isRecursive()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "RECURSIVE", true);
            }
            temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, withItem.getName()));
            if (withItem.getWithItemList() != null) {
                temp.append(this.handleWithGetItemList(withItem));
            }
            if (withItem.isUseValues()) {
                temp.append(this.handleWithGetItemListIsUsingValue(withItem));
            } else {
                SubSelect subSelectWithItem = withItem.getSubSelect();
                if (!subSelectWithItem.isUseBrackets()) {
                    temp.append(OPTIONAL_WHITE_SPACE);
                    temp.append("\\(");
                }
                subSelectWithItem.accept((FromItemVisitor) this);
                if (!subSelectWithItem.isUseBrackets()) {
                    temp.append(OPTIONAL_WHITE_SPACE);
                    temp.append("\\)");
                }
            }
            withItemStringList.add(temp.toString());
        }
        return withItemStringList;
    }

    /**
     * Deparses fetch statement.
     * @param fetch {@link Fetch}
     */
    @Override
    public void deparseFetch(Fetch fetch) {
        buffer.append(REQUIRED_WHITE_SPACE);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "FETCH", true);
        if (fetch.isFetchParamFirst()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "FIRST", true);
        } else {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "NEXT", true);
        }
        if (fetch.getFetchJdbcParameter() != null) {
            buffer.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake,
                    fetch.getFetchJdbcParameter().toString()));
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(fetch.getFetchParam()).append(OPTIONAL_WHITE_SPACE)
                .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ONLY"));

    }

    /**
     * Deparses the whole {@link PlainSelect} object.
     * {@link SuppressWarnings}: PMD.CyclomaticComplexity, PMD.ExcessiveMethodLength and PMD.NPathComplexity
     * @param plainSelect {@link PlainSelect}
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.isUseBrackets()) {
            buffer.append("\\(" + OPTIONAL_WHITE_SPACE);
        }

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "SELECT", true);

        if (plainSelect.getMySqlHintStraightJoin()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "STRAIGHT_JOIN", true);
        }

        OracleHint hint = plainSelect.getOracleHint();
        if (hint != null) {
            buffer.append(hint).append(REQUIRED_WHITE_SPACE);
        }

        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            buffer.append(skip).append(REQUIRED_WHITE_SPACE);
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            buffer.append(first).append(REQUIRED_WHITE_SPACE);
        }

        if (plainSelect.getDistinct() != null) {
            if (plainSelect.getDistinct().isUseUnique()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "UNIQUE", true);
            } else {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "DISTINCT", true);
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "ON", true);
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append("(").append(OPTIONAL_WHITE_SPACE);

                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter
                        .hasNext(); ) {
                    SelectItem selectItem = iter.next();
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(",");
                        buffer.append(OPTIONAL_WHITE_SPACE);
                    }
                }
                buffer.append(OPTIONAL_WHITE_SPACE).append(")");
                buffer.append(REQUIRED_WHITE_SPACE);
            }

        }

        Top top = plainSelect.getTop();
        if (top != null) {
            buffer.append(top).append(OPTIONAL_WHITE_SPACE);
        }

        if (plainSelect.getMySqlSqlCacheFlag() != null) {
            buffer.append(plainSelect.getMySqlSqlCacheFlag().name()).append(OPTIONAL_WHITE_SPACE);
        }

        if (plainSelect.getMySqlSqlCalcFoundRows()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "SQL_CALC_FOUND_ROWS", false);
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<String> selectedColumnNamesAsStrings = new ArrayList<>();
        if (plainSelect.getSelectItems().get(0) instanceof AllColumns) {
            plainSelect.getSelectItems().get(0).accept(this);
        } else {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectedColumnNamesAsStrings.add(this.handleAliasAndAggregateFunction(selectItem));
            }
            buffer.append(OrderRotation.useOrDefault(
                    this.columnNameOrder,
                    selectedColumnNamesAsStrings)
            );
        }
        if (plainSelect.getIntoTables() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FROM", true);

            List<String> selectedTableNamesAsStrings = new ArrayList<>();
            for (Table table : plainSelect.getIntoTables()) {
                String temp = QUOTATION_MARK_REGEX_ZERO_ONE + SpellingMistake.useOrDefault(this.tableNameSpellingMistake, table.getFullyQualifiedName()) + QUOTATION_MARK_REGEX_ZERO_ONE;
                temp = temp + (table.getAlias() != null ? REQUIRED_WHITE_SPACE + SpellingMistake.useOrDefault(this.tableNameSpellingMistake, table.getAlias().toString()) : "");
                selectedTableNamesAsStrings.add(temp);
            }
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, selectedTableNamesAsStrings));
        }

        if (plainSelect.getFromItem() != null && plainSelect.getJoins() != null) {
            List<String> simpleJoinElements = new ArrayList<>();
            StringBuilder fromItemWithAlias = new StringBuilder();
            this.expressionDeParserForRegEx.addTableNameAlias(plainSelect.getFromItem().toString().replaceAll(QUOTATION_MARK_REGEX, ""));
            if(plainSelect.getFromItem().toString().split(" ").length >= 2){
                fromItemWithAlias.append(QUOTATION_MARK_REGEX_ZERO_ONE).append(plainSelect.getFromItem().toString().replaceAll(QUOTATION_MARK_REGEX, "").split(" ")[0]).append(QUOTATION_MARK_REGEX_ZERO_ONE);
                fromItemWithAlias.append("(").append("(?:ALIAS|AS)"+REQUIRED_WHITE_SPACE).append(")?");
                String[] getFromItem = plainSelect.getFromItem().toString().split(" ");
                if(getFromItem.length > 1) {
                    fromItemWithAlias.append(REQUIRED_WHITE_SPACE);
                    fromItemWithAlias.append(QUOTATION_MARK_REGEX_ZERO_ONE);
                    fromItemWithAlias.append(getFromItem[getFromItem.length - 1].replaceAll(QUOTATION_MARK_REGEX, ""));
                    fromItemWithAlias.append(QUOTATION_MARK_REGEX_ZERO_ONE);
                }
            } else {
                fromItemWithAlias.append(QUOTATION_MARK_REGEX_ZERO_ONE)
                        .append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, plainSelect.getFromItem().toString().replaceAll(QUOTATION_MARK_REGEX, "")))
                        .append(QUOTATION_MARK_REGEX_ZERO_ONE);
            }
            simpleJoinElements.add(fromItemWithAlias.toString());


            for (Join join : plainSelect.getJoins()) {
                if (join.isSimple()){
                    this.expressionDeParserForRegEx.addTableNameAlias(join.toString());
                    simpleJoinElements.add(QUOTATION_MARK_REGEX_ZERO_ONE + SpellingMistake.useOrDefault(this.tableNameSpellingMistake, join.toString().replaceAll(QUOTATION_MARK_REGEX, "")) + QUOTATION_MARK_REGEX_ZERO_ONE);
                }
            }

            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FROM", true);

            if (simpleJoinElements.size() == 1) {
                buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, simpleJoinElements));
                for (Join join : plainSelect.getJoins()) {
                    deparseJoin(join);
                }
            } else {
                buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, simpleJoinElements));
            }
        } else if (plainSelect.getFromItem() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FROM", true);
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getKsqlWindow() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "WINDOW", true);
            buffer.append(plainSelect.getKsqlWindow().toString());
        }

        if (plainSelect.getWhere() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "WHERE", true);
            this.expressionDeParserForRegEx.addTableNameAlias(plainSelect.getFromItem().toString());
            plainSelect.getWhere().accept(this.getExpressionDeParserForRegEx());
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionDeParserForRegEx);
        }

        if (plainSelect.getGroupBy() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            GroupByDeParserForRegEx groupByDeParserForRegEx = new GroupByDeParserForRegEx(this.expressionDeParserForRegEx, buffer, settingsContainer);
            if(plainSelect.getFromItem() != null){
                this.expressionDeParserForRegEx.addTableNameAlias(plainSelect.getFromItem().toString());
                groupByDeParserForRegEx.setTableNameAliasMap(this.expressionDeParserForRegEx.getTableNameAliasMap());
            }
            groupByDeParserForRegEx.deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "HAVING", true);
            plainSelect.getHaving().accept(expressionDeParserForRegEx);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(
                    this.getExpressionDeParserForRegEx(),
                    buffer,
                    this.settingsContainer).deParse(
                        plainSelect.getOrderByElements(),
                        plainSelect.getFromItem()
                    );
        }

        if (plainSelect.isEmitChanges()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "EMIT", false);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "CHANGES", false);
        }
        if (plainSelect.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, settingsContainer).deParse(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            deparseOffset(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            deparseFetch(plainSelect.getFetch());
        }
        if (plainSelect.getWithIsolation() != null) {
            buffer.append(plainSelect.getWithIsolation().toString());
        }
        if (plainSelect.isForUpdate()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FOR", false);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "UPDATE", false);
            if (plainSelect.getForUpdateTable() != null) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OF", true);
                buffer.append(plainSelect.getForUpdateTable());
            }

            if (plainSelect.getWait() != null) {
                buffer.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "NOWAIT", false);
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeForForRegEx(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FOR", false);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "XML", true);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "PATH")).append("\\(");
            buffer.append(plainSelect.getForXmlPath()).append(OPTIONAL_WHITE_SPACE + "\\)");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "\\)");
        }

    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link AllTableColumns}.
     * @param allTableColumns {@link AllTableColumns}
     */
    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(allTableColumns.getTable().getFullyQualifiedName())
                .append("." + OPTIONAL_WHITE_SPACE + "(?:ALL|\\*);");
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link SelectExpressionItem}.
     * @param selectExpressionItem {@link SelectExpressionItem}
     */
    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionDeParserForRegEx);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link SubSelect}.
     * @param subSelect {@link SubSelect}
     */
    @Override
    public void visit(SubSelect subSelect) {
        buffer.append(subSelect.isUseBrackets() ? "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "WITH", true);
            buffer.append(this.handleWithItemValueList(subSelect));
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(subSelect.isUseBrackets() ? OPTIONAL_WHITE_SPACE + "\\)" : OPTIONAL_WHITE_SPACE);
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(QUOTATION_MARK_REGEX_ZERO_ONE);
            buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake,
                                                            alias.toString().replace(" ", "")));
            buffer.append(QUOTATION_MARK_REGEX_ZERO_ONE);
        } else {
            buffer.append("(");
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(".*)?");
        }

        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }

        UnPivot unPivot = subSelect.getUnPivot();
        if (unPivot != null) {
            unPivot.accept(this);
        }
    }


    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link Table}.
     * @param tableName {@link Table}
     */
    @Override
    public void visit(Table tableName) {
        buffer.append(QUOTATION_MARK_REGEX_ZERO_INFINITE);
        buffer.append(
                SpellingMistake.useOrDefault(this.tableNameSpellingMistake, tableName.getFullyQualifiedName().replaceAll(QUOTATION_MARK_REGEX, "")));
        buffer.append(QUOTATION_MARK_REGEX_ZERO_INFINITE);
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(QUOTATION_MARK_REGEX_ZERO_INFINITE);
            buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake,
                                                            alias.toString().replaceAll(QUOTATION_MARK_REGEX, "").replace(" ", "")));
            buffer.append(QUOTATION_MARK_REGEX_ZERO_INFINITE);
        } else {
            buffer.append("(");
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(".*)?");
        }

        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        UnPivot unpivot = tableName.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this);
        }
        MySQLIndexHint indexHint = tableName.getIndexHint();
        if (indexHint != null) {
            buffer.append(indexHint);
        }
        SQLServerHints sqlServerHints = tableName.getSqlServerHints();
        if (sqlServerHints != null) {
            buffer.append(sqlServerHints);
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link Pivot}.
     * @param pivot {@link Pivot}
     */
    @Override
    public void visit(Pivot pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(OPTIONAL_WHITE_SPACE);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "PIVOT", false);
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");
        List<String> functionItemList = new LinkedList<>();
        for (FunctionItem functionItem : pivot.getFunctionItems()) {
            functionItemList.add(this.handleAliasAndAggregateFunction(functionItem));
        }
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, functionItemList));
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FOR", true);

        List<String> forColumnsList = new LinkedList<>();
        for (Column column : forColumns) {
            forColumnsList.add(column.toString());
        }
        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, forColumnsList.stream().map(col -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, col)).toList()));
        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "IN", true);

        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        List<String> inItemList = new LinkedList<>();
        for (Object o : pivot.getInItems()) {
            inItemList.add(o.toString());
        }
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, inItemList.stream().map(inItem -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, inItem)).toList()));
        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        buffer.append("\\)");

        if (pivot.getAlias() != null) {
            this.addOptionalAliasKeywords(false);
            buffer.append(pivot.getAlias().toString());
        } else {
            buffer.append("(");
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(".*)?");
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link UnPivot}.
     * @param unpivot {@link UnPivot}
     */
    @Override
    public void visit(UnPivot unpivot) {
        boolean showOptions = unpivot.getIncludeNullsSpecified();
        boolean includeNulls = unpivot.getIncludeNulls();

        List<Column> unPivotClause = unpivot.getUnPivotClause();
        List<String> unPivotClauseAsStringList = new LinkedList<>();
        for (Column col : unPivotClause) {
            unPivotClauseAsStringList.add(col.toString());
        }

        List<Column> unpivotForClause = unpivot.getUnPivotForClause();
        List<String> unPivotForClauseAsStringList = new LinkedList<>();
        for (Column col : unpivotForClause) {
            unPivotForClauseAsStringList.add(col.toString());
        }

        List<SelectExpressionItem> unpivotInClause = unpivot.getUnPivotInClause();
        List<String> unPivotInClauseAsStringList = new LinkedList<>();
        for (SelectExpressionItem selectExpressionItem : unpivotInClause) {
            unPivotInClauseAsStringList.add(selectExpressionItem.toString());
        }

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "UNPIVOT", false);
        if (showOptions && includeNulls) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "INCLUDE", false);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "NULLS", false);
        } else if (showOptions) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "EXCLUDE", false);
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "NULLS", false);
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(unPivotClause.size() > 1 ? "\\(" : "");
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, unPivotClauseAsStringList.stream().map(unPivot -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, unPivot)).toList()));
        buffer.append(unPivotClause.size() > 1 ? "\\)" : "");

        buffer.append(unPivotClause.size() > 1 ? OPTIONAL_WHITE_SPACE : REQUIRED_WHITE_SPACE);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "FOR", true);

        buffer.append(unpivotForClause.size() > 1 ? "\\(" : "");
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, unPivotForClauseAsStringList.stream().map(unPivotFor -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, unPivotFor)).toList()));
        buffer.append(unpivotForClause.size() > 1 ? "\\)" : "");

        buffer.append(unpivotForClause.size() > 1 ? OPTIONAL_WHITE_SPACE : REQUIRED_WHITE_SPACE);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "IN", true);

        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, unPivotInClauseAsStringList.stream().map(unPivotIn -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, unPivotIn)).toList()));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE).append("\\)");

        if (unpivot.getAlias() != null) {
            this.addOptionalAliasKeywords(false);
            buffer.append(unpivot.getAlias().toString());
        } else {
            buffer.append("(");
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(".*)?");
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link PivotXml}.
     * @param pivot {@link PivotXml}
     */
    @Override
    public void visit(PivotXml pivot) {
        List<String> forColumnsAsStringList = new LinkedList<>();
        List<Column> forColumns = pivot.getForColumns();
        for (Column column : forColumns) {
            forColumnsAsStringList.add(column.toString());
        }

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "PIVOT", false);
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "XML", false);
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");

        List<String> functionItemList = new LinkedList<>();
        for (FunctionItem functionItem : pivot.getFunctionItems()) {
            functionItemList.add(this.handleAliasAndAggregateFunction(functionItem));
        }
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, functionItemList));
        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FOR", true);

        buffer.append(forColumns.size() > 1 ? "\\(" : "");
        buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, forColumnsAsStringList.stream().map(forColumn -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, forColumn)).toList()));
        buffer.append(forColumns.size() > 1 ? "\\)" : "");

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "IN", false);
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");

        if (pivot.isInAny()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "ANY", false);
        } else if (pivot.getInSelect() != null) {
            buffer.append(
                    SpellingMistake.useOrDefault(this.tableNameSpellingMistake, pivot.getInSelect().toString()));
        } else {
            List<String> inItemsAsStringList = new LinkedList<>();
            List<?> inItems = pivot.getInItems();
            for (Object o : inItems) {
                inItemsAsStringList.add(o.toString());
            }
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, inItemsAsStringList.stream().map(inItem -> SpellingMistake.useOrDefault(this.tableNameSpellingMistake, inItem)).toList()));
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link SubJoin}.
     * @param subjoin {@link SubJoin}
     */
    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("\\(");
        subjoin.getLeft().accept(this);
        for (Join join : subjoin.getJoinList()) {
            this.deparseJoin(join);
        }
        buffer.append("\\)");

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link SetOperationList}.
     * @param list {@link SetOperationList}
     */
    @Override
    public void visit(SetOperationList list) {
        for (int i = 0; i < list.getSelects().size(); i++) {
            if (i != 0) {
                buffer.append(REQUIRED_WHITE_SPACE).append(list.getOperations().get(i - 1))
                        .append(REQUIRED_WHITE_SPACE);
            }

            boolean brackets = list.getBrackets() == null || list.getBrackets().get(i);
            if (brackets) {
                buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            }

            list.getSelects().get(i).accept(this);
            if (brackets) {
                buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
            }
        }

        if (list.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(expressionDeParserForRegEx, buffer, this.settingsContainer).deParse(
                    list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            new LimitDeParserForRegEx(buffer).deParse(list.getLimit());
        }

        if (list.getOffset() != null) {
            this.deparseOffset(list.getOffset());
        }

        if (list.getFetch() != null) {
            this.deparseFetch(list.getFetch());
        }

        if (list.getWithIsolation() != null) {
            buffer.append(list.getWithIsolation().toString());
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link WithItem}.
     * @param withItem {@link WithItem}
     * @throws UnsupportedOperationException not supported in this implementation
     */
    @Override
    public void visit(WithItem withItem) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link LateralSubSelect}.
     * @param lateralSubSelect {@link LateralSubSelect}
     */
    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link ValuesList}.
     * @param valuesList {@link ValuesList}
     */
    @Override
    public void visit(ValuesList valuesList) {
        buffer.append(valuesList.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link AllColumns}.
     * @param allColumns {@link AllColumns}
     */
    @Override
    public void visit(AllColumns allColumns) {
        buffer.append("(?:").append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ALL")).append("|\\*)");
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link TableFunction}.
     * @param tableFunction {@link TableFunction}
     */
    @Override
    public void visit(TableFunction tableFunction) {
        buffer.append(tableFunction.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link ParenthesisFromItem}.
     * @param parenthesis {@link ParenthesisFromItem}
     */
    @Override
    public void visit(ParenthesisFromItem parenthesis) {
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
        parenthesis.getFromItem().accept(this);

        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        if (parenthesis.getAlias() != null) {
            buffer.append(parenthesis.getAlias().toString());
        }
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link ValuesStatement}.
     * @param values {@link ValuesStatement}
     */
    @Override
    public void visit(ValuesStatement values) {
        new ValuesStatementDeParser(this, buffer).deParse(values);
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link ExpressionList}.
     * @param expressionList {@link ExpressionList}
     */
    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(expressionList.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link NamedExpressionList}.
     * @param namedExpressionList {@link NamedExpressionList}
     */
    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        buffer.append(namedExpressionList.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link MultiExpressionList}.
     * @param multiExprList {@link MultiExpressionList}
     */
    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(multiExprList.toString());
    }

    /**
     * Deparses {@link net.sf.jsqlparser.statement.Statement} instanceof {@link WithItem}.
     * @param withItem {@link WithItem}
     * @return generated regex
     */
    private String handleWithGetItemList(WithItem withItem){
        StringBuilder temp = new StringBuilder();
        temp.append(REQUIRED_WHITE_SPACE);
        List<String> withItemStringListForSelectItem = new LinkedList<>();
        for(SelectItem selectItem : withItem.getWithItemList()){
            withItemStringListForSelectItem.add(selectItem.toString());
        }
        temp.append(OrderRotation.useOrDefault(this.columnNameOrder, withItemStringListForSelectItem));
        return temp.toString();
    }

    /**
     * Extracts value list of {@link WithItem} from a specific {@link net.sf.jsqlparser.statement.Statement} object.
     * @param insert {@link Insert}
     * @return generated regex for value list
     */
    public String handleWithItemValueList(Insert insert){
        return OrderRotation.useOrDefault(this.columnNameOrder, helperFunctionForHandleWithItemValueList(insert.getWithItemsList()));
    }

    /**
     * Performs keyword spelling mistakes with required whitespaces as suffix and prefix.
     * @param whiteSpaceBefore boolean for whitespace before
     * @param keyword keyword to handle by keyword spelling mistake {@link SpellingMistake}
     * @param whiteSpaceAfter boolean for whitespace after
     */
    private void setKeywordSpellingMistakeWithRequiredWhitespaces(boolean whiteSpaceBefore, String keyword, boolean whiteSpaceAfter){
        buffer.append(whiteSpaceBefore ? REQUIRED_WHITE_SPACE : "");
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, keyword));
        buffer.append(whiteSpaceAfter ? REQUIRED_WHITE_SPACE : "");
    }
}
