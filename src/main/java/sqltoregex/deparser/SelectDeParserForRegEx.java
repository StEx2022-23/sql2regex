package sqltoregex.deparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.ValuesStatementDeParser;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SelectDeParserForRegEx extends SelectDeParser {
    public static final String INNER = "INNER";
    public static final String RECURSIVE = "RECURSIVE";
    public static final String SQL_CALC_FOUND_ROWS = "SQL_CALC_FOUND_ROWS";
    public static final String UPDATE = "UPDATE";
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private static final String ALIAS = "ALIAS";
    private static final String AS = "AS";
    public static final String NOT = "NOT";
    public static final String FROM = "FROM";
    public static final String HAVING = "HAVING";
    public static final String EMIT = "EMIT";
    public static final String CHANGES = "CHANGES";
    public static final String SELECT = "SELECT";
    public static final String STRAIGHT_JOIN = "STRAIGHT_JOIN";
    public static final String UNIQUE = "UNIQUE";
    public static final String DISTINCT = "DISTINCT";
    public static final String ON = "ON";
    public static final String WINDOW = "WINDOW";
    public static final String WHERE = "WHERE";
    public static final String OF = "OF";
    public static final String NOWAIT = "NOWAIT";
    public static final String XML = "XML";
    public static final String OFFSET = "OFFSET";
    public static final String RIGHT = "RIGHT";
    public static final String NATURAL = "NATURAL";
    public static final String FULL = "FULL";
    public static final String LEFT = "LEFT";
    public static final String CROSS = "CROSS";
    public static final String OUTER = "OUTER";
    public static final String SEMI = "SEMI";
    public static final String APPLY = "APPLY";
    public static final String JOIN = "JOIN";
    public static final String WITHIN = "WITHIN";
    public static final String USING = "USING";
    public static final String WITH = "WITH";
    public static final String VALUES = "VALUES";
    public static final String PATH = "PATH";
    public static final String FOR = "FOR";
    public static final String IN = "IN";
    private ExpressionVisitor expressionVisitor;
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<String> columnNameSpellingMistake;
    private RegExGenerator<String> tableNameSpellingMistake;
    private RegExGenerator<List<String>> columnNameOrder;
    private RegExGenerator<List<String>> tableNameOrder;
    private RegExGenerator<String> aggregateFunctionLang;
    SettingsManager settingsManager;

    public SelectDeParserForRegEx(SettingsManager settingsManager) {
        super();
        this.settingsManager = settingsManager;
        this.expressionVisitor = new ExpressionDeParserForRegEx(this, buffer, settingsManager);
        this.setKeywordSpellingMistake(settingsManager);
        this.setColumnNameSpellingMistake(settingsManager);
        this.setAggregateFunctionLang(settingsManager);
        this.setColumnNameOrder(settingsManager);
        this.setTableNameOrder(settingsManager);
        this.setTableNameSpellingMistake(settingsManager);
    }

    private void setKeywordSpellingMistake(SettingsManager settingsManager){
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
    }

    private String useKeywordSpellingMistake(String str){
        if(null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setColumnNameSpellingMistake(SettingsManager settingsManager){
        this.columnNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMESPELLING, SpellingMistake.class);
    }

    private String useColumnNameSpellingMistake(String str){
        if(null != this.columnNameSpellingMistake) return this.columnNameSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setTableNameSpellingMistake(SettingsManager settingsManager){
        this.tableNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING, SpellingMistake.class);
    }

    private String useTableNameSpellingMistake(String str) {
        if (null != this.tableNameSpellingMistake) return this.tableNameSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setAggregateFunctionLang(SettingsManager settingsManager){
        this.aggregateFunctionLang = settingsManager.getSettingBySettingsOption(SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class);
    }

    private String useAggregateFunctionLang(String str){
        if(null != this.aggregateFunctionLang) return this.aggregateFunctionLang.generateRegExFor(str);
        else return str;
    }

    private void setColumnNameOrder(SettingsManager settingsManager){
        this.columnNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
    }

    private String useColumnNameOrder(List<String> strlist){
        if(null != this.columnNameOrder) return this.columnNameOrder.generateRegExFor(strlist);
        else return this.combineCommaDividedStringList(strlist);
    }

    private void setTableNameOrder(SettingsManager settingsManager){
        this.tableNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER, OrderRotation.class);
    }

    private String useTableNameOrder(List<String> strlist){
        if(null != this.tableNameOrder) return this.tableNameOrder.generateRegExFor(strlist);
        else return this.combineCommaDividedStringList(strlist);
    }

    private String combineCommaDividedStringList(List<String> strlist){
        StringBuilder str = new StringBuilder();
        Iterator<String> stringListIterator = strlist.iterator();
        while (stringListIterator.hasNext()){
            str.append(stringListIterator.next());
            if (stringListIterator.hasNext()) str.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
        }
        return str.toString();
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.isUseBrackets()) {
            buffer.append("(" + OPTIONAL_WHITE_SPACE);
        }

        buffer.append(useKeywordSpellingMistake(SELECT));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (plainSelect.getMySqlHintStraightJoin()) {
            buffer.append(useKeywordSpellingMistake(STRAIGHT_JOIN));
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
                buffer.append(useKeywordSpellingMistake(UNIQUE));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(useKeywordSpellingMistake(DISTINCT));
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append(useKeywordSpellingMistake(ON));
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append("(");

                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter
                        .hasNext();) {
                    SelectItem selectItem = iter.next();
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(",");
                        buffer.append(OPTIONAL_WHITE_SPACE);
                    }
                }
                buffer.append(")");
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
            buffer.append(useKeywordSpellingMistake(SQL_CALC_FOUND_ROWS));
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        boolean flagForOrderRotationWithOutSpellingMistake = false;
        List<String> selectedColumnNamesAsStrings = new ArrayList<>();
        if(plainSelect.getSelectItems().get(0) instanceof AllColumns){
            plainSelect.getSelectItems().get(0).accept(this);
        }  else {
                for(SelectItem selectItem : plainSelect.getSelectItems()){
                    StringBuilder temp = new StringBuilder();
                    if(selectItem.toString().contains("(") && selectItem.toString().contains(")")){
                        temp.append(useAggregateFunctionLang(selectItem.toString().replaceAll("\\(.*", "")));
                        temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);
                        temp.append(useColumnNameSpellingMistake(selectItem.toString().split("\\(")[1].split("\\)")[0]));
                        temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
                    }

                    if(!selectItem.toString().contains(AS) && !selectItem.toString().contains("(") && !selectItem.toString().contains(")")){
                        temp.append(useColumnNameSpellingMistake(selectItem.toString()));
                        flagForOrderRotationWithOutSpellingMistake = true;
                    }

                    if(selectItem.toString().contains(AS) && selectItem.toString().contains("(") && selectItem.toString().contains(")")) {
                        temp.append(this.addOptionalAliasKeywords(false));
                        temp.append(selectItem.toString().split(AS)[1].replace(" ", ""));
                        flagForOrderRotationWithOutSpellingMistake = true;
                    }

                    if(selectItem.toString().contains(AS) && !selectItem.toString().contains("(") && !selectItem.toString().contains(")")) {
                        temp.append(selectItem.toString().split(AS)[0].replace(" ", ""));
                        temp.append(this.addOptionalAliasKeywords(false));
                        temp.append(selectItem.toString().split(AS)[1].replace(" ", ""));
                        flagForOrderRotationWithOutSpellingMistake = true;
                    }

                    if(!selectItem.toString().contains(AS)){
                        temp.append(OPTIONAL_WHITE_SPACE);
                        temp.append("(");
                        temp.append(this.addOptionalAliasKeywords(false));
                        temp.append(".*)?");
                        flagForOrderRotationWithOutSpellingMistake = true;
                    }

                    selectedColumnNamesAsStrings.add(temp.toString());
                }

                if(flagForOrderRotationWithOutSpellingMistake){
                    List<String> selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake = new ArrayList<>();
                    for(String str : selectedColumnNamesAsStrings){
                        selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake.add(str.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
                    }
                    buffer.append(useColumnNameOrder(selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake));
                } else {
                    buffer.append(useColumnNameOrder(selectedColumnNamesAsStrings));
                }
            }


        if (plainSelect.getIntoTables() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(FROM));
            buffer.append(REQUIRED_WHITE_SPACE);

            List<String> selectedTableNamesAsStrings = new ArrayList<>();
            for (Table table : plainSelect.getIntoTables()) {
                selectedTableNamesAsStrings.add(table.getFullyQualifiedName());
            }
            buffer.append(useTableNameOrder(selectedTableNamesAsStrings));
        }

        if (plainSelect.getFromItem() != null && plainSelect.getJoins() != null) {
            List<String> simpleJoinElements = new ArrayList<>();
            simpleJoinElements.add(plainSelect.getFromItem().toString());

            for (Join join : plainSelect.getJoins()) {
                if(join.isSimple()) simpleJoinElements.add(join.toString());
            }

            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(FROM));
            buffer.append(REQUIRED_WHITE_SPACE);

            if(simpleJoinElements.size() == 1){
                buffer.append(useTableNameOrder(simpleJoinElements));
                for (Join join : plainSelect.getJoins()) {
                    deparseJoin(join);
                }
            } else {
                buffer.append(useTableNameOrder(simpleJoinElements));
            }
        } else if (plainSelect.getFromItem() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(FROM));
            buffer.append(REQUIRED_WHITE_SPACE);
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getKsqlWindow() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE + WINDOW + REQUIRED_WHITE_SPACE);
            buffer.append(plainSelect.getKsqlWindow().toString());
        }

        if (plainSelect.getWhere() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE + WHERE + REQUIRED_WHITE_SPACE);
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
            buffer.append(useKeywordSpellingMistake(HAVING));
            buffer.append(REQUIRED_WHITE_SPACE);
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(this.getExpressionVisitor(), buffer, this.settingsManager).deParse(plainSelect.isOracleSiblings(),
                    plainSelect.getOrderByElements());
        }

        if (plainSelect.isEmitChanges()){
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(EMIT));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(CHANGES));
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
            buffer.append(FOR);
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(UPDATE);
            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(REQUIRED_WHITE_SPACE + OF + REQUIRED_WHITE_SPACE).append(plainSelect.getForUpdateTable());
            }

            if (plainSelect.getWait() != null) {
                buffer.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                buffer.append(REQUIRED_WHITE_SPACE + NOWAIT);
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeForForRegEx(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake(FOR));
            buffer.append(REQUIRED_WHITE_SPACE).append(XML).append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(PATH)).append("\\(");
            buffer.append(plainSelect.getForXmlPath()).append(OPTIONAL_WHITE_SPACE + "\\)");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "\\)");
        }

    }
    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(allTableColumns.getTable().getFullyQualifiedName()).append("." + OPTIONAL_WHITE_SPACE + "(?:ALL|\\*);");
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
            buffer.append(useKeywordSpellingMistake(WITH));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.handleWithItemValueList(subSelect));
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(subSelect.isUseBrackets() ? OPTIONAL_WHITE_SPACE + "\\)" : OPTIONAL_WHITE_SPACE);
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(useTableNameSpellingMistake(alias.toString().replace(" ", "")));
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
        buffer.append(useTableNameSpellingMistake(tableName.getFullyQualifiedName()));
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(addOptionalAliasKeywords(true));
            buffer.append(useTableNameSpellingMistake(alias.toString().replace(" ", "")));
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
        buffer.append(useKeywordSpellingMistake("PIVOT"));
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");
            List<String> functionItemList = new LinkedList<>();
            for(FunctionItem functionItem : pivot.getFunctionItems()){
                functionItemList.add(functionItem.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            // TODO: handle aggregatefunction and alias, refactor method below to reuse
            buffer.append(useColumnNameOrder(functionItemList));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(FOR));
            buffer.append(REQUIRED_WHITE_SPACE);

            List<String> forColumnsList = new LinkedList<>();
            for(Column column : forColumns){
                forColumnsList.add(column.toString());
            }
            buffer.append(forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
            buffer.append(useColumnNameOrder(forColumnsList));
            buffer.append(forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);

            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(IN));
            buffer.append(REQUIRED_WHITE_SPACE);

            buffer.append(forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
            List<String> inItemList = new LinkedList<>();
            for (Object o : pivot.getInItems()) {
                inItemList.add(o.toString());
            }
            buffer.append(useColumnNameOrder(inItemList));
            buffer.append(forColumnsList.size() > 1 ? OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE);
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
        List<Column> unpivotForClause = unpivot.getUnPivotForClause();
        buffer
                .append(" UNPIVOT")
                .append(showOptions && includeNulls ? " INCLUDE NULLS" : "")
                .append(showOptions && !includeNulls ? " EXCLUDE NULLS" : "")
                .append(" (").append(PlainSelect.getStringList(unPivotClause, true,
                        unPivotClause != null && unPivotClause.size() > 1))
                .append(" FOR ").append(PlainSelect.getStringList(unpivotForClause, true,
                        unpivotForClause != null && unpivotForClause.size() > 1))
                .append(" IN ").append(PlainSelect.getStringList(unpivot.getUnPivotInClause(), true, true)).append(")");
        if (unpivot.getAlias() != null) {
            buffer.append(unpivot.getAlias().toString());
        }
    }

    @Override
    public void visit(PivotXml pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT XML (").append(PlainSelect.getStringList(pivot.getFunctionItems())).append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN (");
        if (pivot.isInAny()) {
            buffer.append("ANY");
        } else if (pivot.getInSelect() != null) {
            buffer.append(pivot.getInSelect());
        } else {
            buffer.append(PlainSelect.getStringList(pivot.getInItems()));
        }
        buffer.append("))");
    }

    @Override
    public void deparseOffset(Offset offset) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(useKeywordSpellingMistake(OFFSET));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(offset.getOffset());
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (offset.getOffsetParam() != null) {
            buffer.append(useKeywordSpellingMistake(offset.getOffsetParam()));
        }
    }

    @Override
    public void deparseFetch(Fetch fetch) {
        // FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
        buffer.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            buffer.append("FIRST ");
        } else {
            buffer.append("NEXT ");
        }
        if (fetch.getFetchJdbcParameter() != null) {
            buffer.append(fetch.getFetchJdbcParameter().toString());
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(" ").append(fetch.getFetchParam()).append(" ONLY");

    }

    @Override
    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    @Override
    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("(");
        subjoin.getLeft().accept(this);
        for (Join join : subjoin.getJoinList()) {
            deparseJoin(join);
        }
        buffer.append(")");

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
            buffer.append(useKeywordSpellingMistake(OUTER));
            buffer.append(REQUIRED_WHITE_SPACE);
        } else if (join.isSimple()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
        } else {
            if (join.isRight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(RIGHT));
            } else if (join.isNatural()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(NATURAL));
            } else if (join.isFull()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(FULL));
            } else if (join.isLeft()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(LEFT));
            } else if (join.isCross()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(CROSS));
            }

            if (join.isOuter()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(OUTER));
            } else if (join.isInner()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(INNER));
            } else if (join.isSemi()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(SEMI));
            }

            if (join.isStraight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(STRAIGHT_JOIN));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else if (join.isApply()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(APPLY));
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(JOIN));
                buffer.append(REQUIRED_WHITE_SPACE);
            }
        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(WITHIN));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(ON));
            buffer.append(REQUIRED_WHITE_SPACE);
            onExpression.accept(expressionVisitor);
        }
        if (!join.getUsingColumns().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(useKeywordSpellingMistake(USING));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("(");
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
            buffer.append(")");
        }
    }

    @Override
    public void visit(SetOperationList list) {
        for (int i = 0; i < list.getSelects().size(); i++) {
            if (i != 0) {
                buffer.append(' ').append(list.getOperations().get(i - 1)).append(' ');
            }

            boolean brackets = list.getBrackets() == null || list.getBrackets().get(i);
            if (brackets) {
                buffer.append("(");
            }

            list.getSelects().get(i).accept(this);
            if (brackets) {
                buffer.append(")");
            }
        }

        if (list.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(expressionVisitor, buffer, this.settingsManager).deParse(list.getOrderByElements());
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
        buffer.append("(");
        parenthesis.getFromItem().accept(this);

        buffer.append(")");
        if (parenthesis.getAlias() != null) {
            buffer.append(parenthesis.getAlias().toString());
        }
    }

    @Override
    public void visit(ValuesStatement values) {
        new ValuesStatementDeParser(this, buffer).deParse(values);
    }

    private void deparseOptimizeForForRegEx(OptimizeFor optimizeFor) {
        buffer.append(" OPTIMIZE FOR ");
        buffer.append(optimizeFor.getRowCount());
        buffer.append(" ROWS");
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
        temp.append(useKeywordSpellingMistake(ALIAS));
        temp.append("|");
        temp.append(useKeywordSpellingMistake(AS));
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
        temp.append(useKeywordSpellingMistake(VALUES));
        temp.append(REQUIRED_WHITE_SPACE);
        temp.append("(VALUES ");
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
                temp.append(useKeywordSpellingMistake(RECURSIVE));
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
}
