package sqltoregex.deparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.ValuesStatementDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SelectDeParserForRegEx extends SelectDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private boolean flagForOrderRotationWithOutSpellingMistake = false;
    private ExpressionVisitor expressionVisitor;
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<String> columnNameSpellingMistake;
    private RegExGenerator<String> tableNameSpellingMistake;
    private RegExGenerator<List<String>> columnNameOrder;
    private RegExGenerator<List<String>> tableNameOrder;
    private RegExGenerator<String> aggregateFunctionLang;
    SettingsManager settingsManager;
    private ExpressionDeParserForRegEx expressionVisitor;
    private boolean flagForOrderRotationWithOutSpellingMistake = false;

    public SelectDeParserForRegEx(SettingsManager settingsManager) {
        super();
        this.settingsManager = settingsManager;
        this.expressionVisitor = new ExpressionDeParserForRegEx(this, buffer, settingsManager);
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                                                                                 SpellingMistake.class).orElse(null);
        this.columnNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMESPELLING,
                                                                                    SpellingMistake.class).orElse(null);
        this.aggregateFunctionLang = settingsManager.getSettingBySettingsOption(SettingsOption.AGGREGATEFUNCTIONLANG,
                                                                                StringSynonymGenerator.class)
                .orElse(null);
        this.columnNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER,
                                                                          OrderRotation.class).orElse(null);
        this.tableNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER,
                                                                         OrderRotation.class).orElse(null);
        this.tableNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING,
                                                                                   SpellingMistake.class).orElse(null);
    }

    public String addOptionalAliasKeywords(boolean isOptional) {
        StringBuilder temp = new StringBuilder();
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("(?:");
        temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ALIAS));
        temp.append("|");
        temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, AS));
        if (isOptional) temp.append(")?");
        else temp.append(")");
        temp.append(REQUIRED_WHITE_SPACE);
        return temp.toString();
    }

    @Override
    public void deparseFetch(Fetch fetch) {
        buffer.append(REQUIRED_WHITE_SPACE)
                .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FETCH))
                .append(REQUIRED_WHITE_SPACE);
        if (fetch.isFetchParamFirst()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FIRST))
                    .append(REQUIRED_WHITE_SPACE);
        } else {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NEXT))
                    .append(REQUIRED_WHITE_SPACE);
        }
        if (fetch.getFetchJdbcParameter() != null) {
            buffer.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake,
                                                            fetch.getFetchJdbcParameter().toString()));
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(fetch.getFetchParam()).append(OPTIONAL_WHITE_SPACE)
                .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ONLY));

    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deparseJoin(Join join) {
        if (join.isSimple() && join.isOuter()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, OUTER));
            buffer.append(REQUIRED_WHITE_SPACE);
        } else if (join.isSimple()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
        } else {
            if (join.isRight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, RIGHT));
            } else if (join.isNatural()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NATURAL));
            } else if (join.isFull()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FULL));
            } else if (join.isLeft()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, LEFT));
            } else if (join.isCross()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, CROSS));
            }

            if (join.isOuter()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, OUTER));
            } else if (join.isInner()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, INNER));
            } else if (join.isSemi()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, SEMI));
            }

            if (join.isStraight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, STRAIGHT_JOIN));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else if (join.isApply()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, APPLY));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, JOIN));
                buffer.append(REQUIRED_WHITE_SPACE);
            }
        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, WITHIN));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ON));
            buffer.append(REQUIRED_WHITE_SPACE);
            onExpression.accept(expressionVisitor);
        }
        if (!join.getUsingColumns().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, USING));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE);
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext(); ) {
                Column column = iterator.next();
                buffer.append(column.toString());
                if (iterator.hasNext()) {
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                }
            }
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        }
    }

    @Override
    public void deparseOffset(Offset offset) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, OFFSET));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(offset.getOffset());
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (offset.getOffsetParam() != null) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, offset.getOffsetParam()));
        }
    }

    private void deparseOptimizeForForRegEx(OptimizeFor optimizeFor) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, OPTIMIZE));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FOR));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(optimizeFor.getRowCount());
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ROWS));
    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        if (!(visitor instanceof ExpressionDeParserForRegEx)) {
            throw new IllegalArgumentException(
                    "ExpressionVisitor must be of type ExpressionDeParserForRegex for this implementation");
        }
        expressionVisitor = (ExpressionDeParserForRegEx) visitor;
    }

    private String handleALiasAndAggregateFunction(Object o) {
        StringBuilder temp = new StringBuilder();
        if (o.toString().contains("(") && o.toString().contains(")")) {
            temp.append(RegExGenerator.useStringSynonymGenerator(this.aggregateFunctionLang,
                                                                 o.toString().replaceAll("\\(.*", "")));
            temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);
            temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake,
                                                          o.toString().split("\\(")[1].split("\\)")[0]));
            temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
        }

        if (!o.toString().contains(AS) && !o.toString().contains("(") && !o.toString().contains(")")) {
            temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, o.toString()));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if (o.toString().contains(AS) && o.toString().contains("(") && o.toString().contains(")")) {
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(o.toString().split(AS)[1].replace(" ", ""));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if (o.toString().contains(AS) && !o.toString().contains("(") && !o.toString().contains(")")) {
            temp.append(o.toString().split(AS)[0].replace(" ", ""));
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(o.toString().split(AS)[1].replace(" ", ""));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if (!o.toString().contains(AS)) {
            temp.append(OPTIONAL_WHITE_SPACE);
            temp.append("(");
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(".*)?");
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }
        return temp.toString();
    }

    private String handleOrderRotationWithExplicitNoneSpellingMistake(List<String> stringList) {
        StringBuilder temp = new StringBuilder();
        if (this.flagForOrderRotationWithOutSpellingMistake) {
            List<String> selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake = new ArrayList<>();
            for (String str : stringList) {
                selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake.add(
                        str.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            temp.append(RegExGenerator.useOrderRotation(this.columnNameOrder,
                                                        selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake));
        } else {
            temp.append(RegExGenerator.useOrderRotation(this.columnNameOrder, stringList));
        }
        return temp.toString();
    }

    private String useColumnNameOrder(List<String> strlist){
        if(null != this.columnNameOrder) return this.columnNameOrder.generateRegExFor(strlist);
        else return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, strlist);
    }

    private String handleWithGetItemListIsUsingValue(WithItem withItem) {
        StringBuilder temp = new StringBuilder();
        ItemsList itemsList = withItem.getItemsList();
        temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, VALUES));
        temp.append(REQUIRED_WHITE_SPACE);
        temp.append("\\(").append(OPTIONAL_WHITE_SPACE).append(VALUES);
        temp.append(REQUIRED_WHITE_SPACE);
        ExpressionList expressionList = (ExpressionList) itemsList;
        Iterator<Expression> expressionIterator = expressionList.getExpressions().iterator();
        List<String> expressionListAsStrings = new LinkedList<>();
        while (expressionIterator.hasNext()) {
            Expression selectItem = expressionIterator.next();
            expressionListAsStrings.add(
                    selectItem.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        temp.append(RegExGenerator.useOrderRotation(this.columnNameOrder, expressionListAsStrings));
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("\\)");
        return temp.toString();
    }

    public String handleWithItemValueList(Select select) {
        return RegExGenerator.useOrderRotation(this.tableNameOrder,
                                               helperFunctionForHandleWithItemValueList(select.getWithItemsList()));
    }

    public String handleWithItemValueList(SubSelect select) {
        return RegExGenerator.useOrderRotation(this.tableNameOrder,
                                               helperFunctionForHandleWithItemValueList(select.getWithItemsList()));
    }

    private List<String> helperFunctionForHandleWithItemValueList(List<WithItem> listOfWithItems) {
        List<String> withItemStringList = new LinkedList<>();
        for (WithItem withItem : listOfWithItems) {
            StringBuilder temp = new StringBuilder();
            if (withItem.isRecursive()) {
                temp.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, RECURSIVE));
                temp.append(REQUIRED_WHITE_SPACE);
            }
            temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, withItem.getName()));
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
            withItemStringList.add(temp.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        return withItemStringList;
    }

    private void setFlagForOrderRotationWithOutSpellingMistake(boolean flag) {
        this.flagForOrderRotationWithOutSpellingMistake = flag;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.isUseBrackets()) {
            buffer.append("\\(" + OPTIONAL_WHITE_SPACE);
        }

        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, SELECT));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (plainSelect.getMySqlHintStraightJoin()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, STRAIGHT_JOIN));
            buffer.append(REQUIRED_WHITE_SPACE);
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
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, UNIQUE));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, DISTINCT));
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ON));
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
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, SQL_CALC_FOUND_ROWS));
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        this.setFlagForOrderRotationWithOutSpellingMistake(false);
        List<String> selectedColumnNamesAsStrings = new ArrayList<>();
        if (plainSelect.getSelectItems().get(0) instanceof AllColumns) {
            plainSelect.getSelectItems().get(0).accept(this);
        } else {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                selectedColumnNamesAsStrings.add(this.handleALiasAndAggregateFunction(selectItem));
            }
            buffer.append(this.handleOrderRotationWithExplicitNoneSpellingMistake(selectedColumnNamesAsStrings));
        }
        if (plainSelect.getIntoTables() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FROM));
            buffer.append(REQUIRED_WHITE_SPACE);

            List<String> selectedTableNamesAsStrings = new ArrayList<>();
            for (Table table : plainSelect.getIntoTables()) {
                String temp = useTableNameSpellingMistake(table.getFullyQualifiedName());
                temp = temp + (table.getAlias() != null ? REQUIRED_WHITE_SPACE + useTableNameSpellingMistake(table.getAlias().toString()) + DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE : "");
                selectedTableNamesAsStrings.add(temp);
            }
            buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder, selectedTableNamesAsStrings));
        }

        if (plainSelect.getFromItem() != null && plainSelect.getJoins() != null) {
            List<String> simpleJoinElements = new ArrayList<>();
            simpleJoinElements.add(plainSelect.getFromItem().toString());

            for (Join join : plainSelect.getJoins()) {
                if (join.isSimple()) simpleJoinElements.add(join.toString());
            }

            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FROM));
            buffer.append(REQUIRED_WHITE_SPACE);

            if (simpleJoinElements.size() == 1) {
                buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder, simpleJoinElements));
                for (Join join : plainSelect.getJoins()) {
                    deparseJoin(join);
                }
            } else {
                buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder, simpleJoinElements));
            }
        } else if (plainSelect.getFromItem() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FROM));
            buffer.append(REQUIRED_WHITE_SPACE);
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getKsqlWindow() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE + "WINDOW" + REQUIRED_WHITE_SPACE);
            buffer.append(plainSelect.getKsqlWindow().toString());
        }

        if (plainSelect.getWhere() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE + "WHERE" + REQUIRED_WHITE_SPACE);
            plainSelect.getWhere().accept(this.getExpressionVisitor());
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupBy() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            new GroupByDeParserForRegEx(expressionVisitor, buffer, settingsManager).deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, HAVING));
            buffer.append(REQUIRED_WHITE_SPACE);
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(this.getExpressionVisitor(), buffer, this.settingsManager).deParse(plainSelect.isOracleSiblings(),
                    plainSelect.getOrderByElements(), plainSelect.getFromItem());
        }

        if (plainSelect.isEmitChanges()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, EMIT));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, CHANGES));
            buffer.append(OPTIONAL_WHITE_SPACE);
        }
        if (plainSelect.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, settingsManager).deParse(plainSelect.getLimit());
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
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("FOR");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("UPDATE");
            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(REQUIRED_WHITE_SPACE + "OF" + REQUIRED_WHITE_SPACE).append(plainSelect.getForUpdateTable());
            }

            if (plainSelect.getWait() != null) {
                buffer.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                buffer.append(REQUIRED_WHITE_SPACE + "NOWAIT");
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeForForRegEx(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(REQUIRED_WHITE_SPACE)
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FOR));
            buffer.append(REQUIRED_WHITE_SPACE).append(XML).append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, PATH)).append("\\(");
            buffer.append(plainSelect.getForXmlPath()).append(OPTIONAL_WHITE_SPACE + "\\)");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "\\)");
        }

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(allTableColumns.getTable().getFullyQualifiedName())
                .append("." + OPTIONAL_WHITE_SPACE + "(?:ALL|\\*);");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionVisitor);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        buffer.append(subSelect.isUseBrackets() ? "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, WITH));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.handleWithItemValueList(subSelect));
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(subSelect.isUseBrackets() ? OPTIONAL_WHITE_SPACE + "\\)" : OPTIONAL_WHITE_SPACE);
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake,
                                                            alias.toString().replace(" ", "")));
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

    @Override
    public void visit(Table tableName) {
        buffer.append(
                RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, tableName.getFullyQualifiedName()));
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake,
                                                            alias.toString().replace(" ", "")));
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

    @Override
    public void visit(Pivot pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, PIVOT));
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");
        List<String> functionItemList = new LinkedList<>();
        for (FunctionItem functionItem : pivot.getFunctionItems()) {
            functionItemList.add(this.handleALiasAndAggregateFunction(functionItem));
        }
        buffer.append(this.handleOrderRotationWithExplicitNoneSpellingMistake(functionItemList));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FOR));
        buffer.append(REQUIRED_WHITE_SPACE);

        List<String> forColumnsList = new LinkedList<>();
        for (Column column : forColumns) {
            forColumnsList.add(column.toString());
        }
        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, forColumnsList));
        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);

        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, IN));
        buffer.append(REQUIRED_WHITE_SPACE);

        buffer.append(
                forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
        List<String> inItemList = new LinkedList<>();
        for (Object o : pivot.getInItems()) {
            inItemList.add(o.toString());
        }
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, inItemList));
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
        List<String> unpivotInClauseAsStringList = new LinkedList<>();
        for (SelectExpressionItem selectExpressionItem : unpivotInClause) {
            unpivotInClauseAsStringList.add(selectExpressionItem.toString());
        }

        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, UNPIVOT));
        if (showOptions && includeNulls) {
            buffer.append(REQUIRED_WHITE_SPACE)
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, INCLUDE))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NULLS));
        } else if (showOptions) {
            buffer.append(REQUIRED_WHITE_SPACE)
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, EXCLUDE))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, NULLS));
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(unPivotClause.size() > 1 ? "\\(" : "");
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, unPivotClauseAsStringList));
        buffer.append(unPivotClause.size() > 1 ? "\\)" : "");

        buffer.append(unPivotClause.size() > 1 ? OPTIONAL_WHITE_SPACE : REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FOR));
        buffer.append(REQUIRED_WHITE_SPACE);

        buffer.append(unpivotForClause.size() > 1 ? "\\(" : "");
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, unPivotForClauseAsStringList));
        buffer.append(unpivotForClause.size() > 1 ? "\\)" : "");

        buffer.append(unpivotForClause.size() > 1 ? OPTIONAL_WHITE_SPACE : REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, IN));
        buffer.append(REQUIRED_WHITE_SPACE);

        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, unpivotInClauseAsStringList));
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

    @Override
    public void visit(PivotXml pivot) {
        List<String> forColumnsAsStringList = new LinkedList<>();
        List<Column> forColumns = pivot.getForColumns();
        for (Column column : forColumns) {
            forColumnsAsStringList.add(column.toString());
        }
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, PIVOT));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, XML));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");

        List<String> functionItemList = new LinkedList<>();
        for (FunctionItem functionItem : pivot.getFunctionItems()) {
            functionItemList.add(this.handleALiasAndAggregateFunction(functionItem));
        }
        buffer.append(this.handleOrderRotationWithExplicitNoneSpellingMistake(functionItemList));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, FOR));
        buffer.append(REQUIRED_WHITE_SPACE);

        buffer.append(forColumns.size() > 1 ? "\\(" : "");
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, forColumnsAsStringList));
        buffer.append(forColumns.size() > 1 ? "\\)" : "");

        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, IN));
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");

        if (pivot.isInAny()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, ANY));
        } else if (pivot.getInSelect() != null) {
            buffer.append(
                    RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, pivot.getInSelect().toString()));
        } else {
            List<String> inItemsAsStringList = new LinkedList<>();
            List<?> inItems = pivot.getInItems();
            for (Object o : inItems) {
                inItemsAsStringList.add(o.toString());
            }
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder, inItemsAsStringList));
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    @Override
    public void deparseOffset(Offset offset) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake("OFFSET"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(offset.getOffset());
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (offset.getOffsetParam() != null) {
            buffer.append(useKeywordSpellingMistake(offset.getOffsetParam()));
        }
    }

    @Override
    public void deparseFetch(Fetch fetch) {
        buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake("FETCH")).append(REQUIRED_WHITE_SPACE);
        if (fetch.isFetchParamFirst()) {
            buffer.append(useKeywordSpellingMistake("FIRST")).append(REQUIRED_WHITE_SPACE);
        } else {
            buffer.append(useKeywordSpellingMistake("NEXT")).append(REQUIRED_WHITE_SPACE);
        }
        if (fetch.getFetchJdbcParameter() != null) {
            buffer.append(useColumnNameSpellingMistake(fetch.getFetchJdbcParameter().toString()));
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(fetch.getFetchParam()).append(OPTIONAL_WHITE_SPACE).append(useKeywordSpellingMistake("ONLY"));

    }

    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("\\(");
        subjoin.getLeft().accept(this);
        for (Join join : subjoin.getJoinList()) {
            deparseJoin(join);
        }
        buffer.append("\\)");

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deparseJoin(Join join) {
        if (join.isSimple() && join.isOuter()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake("OUTER"));
            buffer.append(REQUIRED_WHITE_SPACE);
        } else if (join.isSimple()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
        } else {
            if (join.isRight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("RIGHT"));
            } else if (join.isNatural()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("NATURAL"));
            } else if (join.isFull()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("FULL"));
            } else if (join.isLeft()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("LEFT"));
            } else if (join.isCross()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("CROSS"));
            }

            if (join.isOuter()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("OUTER"));
            } else if (join.isInner()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("INNER"));
            } else if (join.isSemi()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("SEMI"));
            }

            if (join.isStraight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("STRAIGHT_JOIN"));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else if (join.isApply()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("APPLY"));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake("JOIN"));
                buffer.append(REQUIRED_WHITE_SPACE);
            }
        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake("WITHIN"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake("ON"));
            buffer.append(REQUIRED_WHITE_SPACE);
            onExpression.accept(expressionVisitor);
        }
        if (!join.getUsingColumns().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake("USING"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE);
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext();) {
                Column column = iterator.next();
                buffer.append(column.toString());
                if (iterator.hasNext()) {
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                }
            }
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        }
    }

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
            new OrderByDeParserForRegEx(expressionVisitor, buffer, this.settingsManager).deParse(
                    list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            new LimitDeParserForRegEx(buffer).deParse(list.getLimit());
        }

        if (list.getOffset() != null) {
            deparseOffset(list.getOffset());
        }

        if (list.getFetch() != null) {
            deparseFetch(list.getFetch());
        }

        if (list.getWithIsolation() != null) {
            buffer.append(list.getWithIsolation().toString());
        }
    }

    @Override
    public void visit(WithItem withItem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        buffer.append(valuesList.toString());
    }

    @Override
    public void visit(AllColumns allColumns) {
        buffer.append("(?:ALL|\\*)");
    }

    @Override
    public void visit(TableFunction tableFunction) {
        buffer.append(tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem parenthesis) {
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE);
        parenthesis.getFromItem().accept(this);

        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        if (parenthesis.getAlias() != null) {
            buffer.append(parenthesis.getAlias().toString());
        }
    }

    @Override
    public void visit(ValuesStatement values) {
        new ValuesStatementDeParser(this, buffer).deParse(values);
    }

    private void deparseOptimizeForForRegEx(OptimizeFor optimizeFor) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake("OPTIMIZE"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake("FOR"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(optimizeFor.getRowCount());
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake("ROWS"));
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(expressionList.toString());
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        buffer.append(namedExpressionList.toString());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(multiExprList.toString());
    }

    public String addOptionalAliasKeywords(boolean isOptional){
        StringBuilder temp = new StringBuilder();
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("(?:");
        temp.append(useKeywordSpellingMistake("ALIAS"));
        temp.append("|");
        temp.append(useKeywordSpellingMistake("AS"));
        if(isOptional) temp.append(")?");
        else temp.append(")");
        temp.append(REQUIRED_WHITE_SPACE);
        return temp.toString();
    }

    private String handleWithGetItemList(WithItem withItem){
        StringBuilder temp = new StringBuilder();
        temp.append(REQUIRED_WHITE_SPACE);
        List<String> withItemStringListForSelectItem = new LinkedList<>();
        for(SelectItem selectItem : withItem.getWithItemList()){
            withItemStringListForSelectItem.add(selectItem.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        temp.append(useColumnNameOrder(withItemStringListForSelectItem));
        return temp.toString();
    }

    private String handleWithGetItemListIsUsingValue(WithItem withItem){
        StringBuilder temp = new StringBuilder();
        ItemsList itemsList = withItem.getItemsList();
        temp.append(useKeywordSpellingMistake("VALUES"));
        temp.append(REQUIRED_WHITE_SPACE);
        temp.append("\\(").append(OPTIONAL_WHITE_SPACE).append("VALUES");
        temp.append(REQUIRED_WHITE_SPACE);
        ExpressionList expressionList = (ExpressionList) itemsList;
        Iterator<Expression> expressionIterator = expressionList.getExpressions().iterator();
        List<String> expressionListAsStrings = new LinkedList<>();
        while (expressionIterator.hasNext()){
            Expression selectItem = expressionIterator.next();
            expressionListAsStrings.add(selectItem.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        temp.append(useColumnNameOrder(expressionListAsStrings));
        temp.append(OPTIONAL_WHITE_SPACE);
        temp.append("\\)");
        return temp.toString();
    }


    private List<String> helperFunctionForHandleWithItemValueList(List<WithItem> listOfWithItems){
        List<String> withItemStringList = new LinkedList<>();
        for(WithItem withItem : listOfWithItems){
            StringBuilder temp = new StringBuilder();
            if (withItem.isRecursive()) {
                temp.append(useKeywordSpellingMistake("RECURSIVE"));
                temp.append(REQUIRED_WHITE_SPACE);
            }
            temp.append(useColumnNameSpellingMistake(withItem.getName()));
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
            withItemStringList.add(temp.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        return withItemStringList;
    }


    public String handleWithItemValueList(Select select){
        return useTableNameOrder(helperFunctionForHandleWithItemValueList(select.getWithItemsList()));
    }

    public String handleWithItemValueList(SubSelect select){
        return useTableNameOrder(helperFunctionForHandleWithItemValueList(select.getWithItemsList()));
    }

    public String handleWithItemValueList(Insert insert){
        return useTableNameOrder(helperFunctionForHandleWithItemValueList(insert.getWithItemsList()));
    }

    private String handleALiasAndAggregateFunction(Object o){
        StringBuilder temp = new StringBuilder();
        if(o.toString().contains("(") && o.toString().contains(")")){
            temp.append(useAggregateFunctionLang(o.toString().replaceAll("\\(.*", "")));
            temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);
            temp.append(useColumnNameSpellingMistake(o.toString().split("\\(")[1].split("\\)")[0]));
            temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
        }

        if(!o.toString().contains("AS") && !o.toString().contains("(") && !o.toString().contains(")")){
            temp.append(useColumnNameSpellingMistake(o.toString()));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if(o.toString().contains("AS") && o.toString().contains("(") && o.toString().contains(")")) {
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(o.toString().split("AS")[1].replace(" ", ""));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if(o.toString().contains("AS") && !o.toString().contains("(") && !o.toString().contains(")")) {
            temp.append(o.toString().split("AS")[0].replace(" ", ""));
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(o.toString().split("AS")[1].replace(" ", ""));
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }

        if(!o.toString().contains("AS")){
            temp.append(OPTIONAL_WHITE_SPACE);
            temp.append("(");
            temp.append(this.addOptionalAliasKeywords(false));
            temp.append(".*)?");
            this.setFlagForOrderRotationWithOutSpellingMistake(true);
        }
        return temp.toString();
    }

    private void setFlagForOrderRotationWithOutSpellingMistake(boolean flag){
        this.flagForOrderRotationWithOutSpellingMistake = flag;
    }

    private String handleOrderRotationWithExplicitNoneSpellingMistake(List<String> stringList){
        StringBuilder temp = new StringBuilder();
        if(this.flagForOrderRotationWithOutSpellingMistake){
            List<String> selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake = new ArrayList<>();
            for(String str : stringList){
                selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake.add(str.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            temp.append(useColumnNameOrder(selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake));
        } else {
            temp.append(useColumnNameOrder(stringList));
        }
        return temp.toString();
    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

}
