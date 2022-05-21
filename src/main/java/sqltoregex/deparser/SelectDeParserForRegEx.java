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
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectDeParserForRegEx extends SelectDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private static final String ALIAS = "ALIAS";
    private static final String AS = "AS";
    public static final String NOT = "NOT";
    private ExpressionVisitor expressionVisitor;
    private final boolean isKeywordSpellingMistake;
    private final boolean isColumnNameMistake;
    private RegExGenerator<String> keywordSpellingMistake;
    private RegExGenerator<String> columnNameMistake;
    private final RegExGenerator<List<String>> columnNameOrder;
    private final RegExGenerator<List<String>> tableNameOrder;
    private final RegExGenerator<String> aggregateFunctionLang;
    SettingsManager settingsManager;

    public SelectDeParserForRegEx(SettingsManager settingsManager) {
        super();
        this.settingsManager = settingsManager;
        this.expressionVisitor = new ExpressionDeParserForRegEx(settingsManager);
        this.isKeywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING);
        if(this.isKeywordSpellingMistake){
            keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        }
        columnNameOrder = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
        tableNameOrder = settingsManager.getSettingBySettingOption(SettingsOption.TABLENAMEORDER, OrderRotation.class);
        aggregateFunctionLang = settingsManager.getSettingBySettingOption(SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class);
        this.isColumnNameMistake = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMESPELLING);
        if(this.isColumnNameMistake){
            columnNameMistake = settingsManager.getSettingBySettingOption(SettingsOption.COLUMNNAMESPELLING, SpellingMistake.class);
        }
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.isUseBrackets()) {
            buffer.append("(" + OPTIONAL_WHITE_SPACE);
        }

        buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("SELECT") : "SELECT");
        buffer.append(REQUIRED_WHITE_SPACE);


        if (plainSelect.getMySqlHintStraightJoin()) {
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("STRAIGHT_JOIN") : "STRAIGHT_JOIN");
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
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("UNIQUE") : "UNIQUE");
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("DISTINCT") : "DISTINCT");
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("ON") : "ON");
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
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("SQL_CALC_FOUND_ROWS") : "SQL_CALC_FOUND_ROWS");
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        List<String> selectedColumnNamesAsStrings = new ArrayList<>();

        boolean flagForOrderRotationWithOutSpellingMistake = false;
        for(SelectItem selectItem : plainSelect.getSelectItems()){
            StringBuilder temp = new StringBuilder();

            if(selectItem.toString().contains("(") && selectItem.toString().contains(")")){
                temp.append(aggregateFunctionLang.generateRegExFor(selectItem.toString().replaceAll("\\(.*", "")));
                temp.append(OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE);
                temp.append(isColumnNameMistake ? columnNameMistake.generateRegExFor(selectItem.toString().split("\\(")[1].split("\\)")[0]) : selectItem.toString().split("\\(")[1].split("\\)")[0]);
                temp.append(OPTIONAL_WHITE_SPACE + "\\)" + OPTIONAL_WHITE_SPACE);
            }

            if(!selectItem.toString().contains(AS) && !selectItem.toString().contains("(") && !selectItem.toString().contains(")")){
                temp.append(isColumnNameMistake ? columnNameMistake.generateRegExFor(selectItem.toString()) : selectItem.toString());
                flagForOrderRotationWithOutSpellingMistake = true;
            }

            if(selectItem.toString().contains(AS) && selectItem.toString().contains("(") && selectItem.toString().contains(")")) {
                temp.append(OPTIONAL_WHITE_SPACE);
                temp.append("(?:");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(ALIAS) : ALIAS);
                temp.append("|");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(AS) : AS);
                temp.append(")");
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append(selectItem.toString().split(AS)[1].replace(" ", ""));
                flagForOrderRotationWithOutSpellingMistake = true;
            }

            if(selectItem.toString().contains(AS) && !selectItem.toString().contains("(") && !selectItem.toString().contains(")")) {
                temp.append(selectItem.toString().split(AS)[0].replace(" ", ""));
                temp.append(OPTIONAL_WHITE_SPACE);
                temp.append("(?:");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(ALIAS) : ALIAS);
                temp.append("|");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(AS) : AS);
                temp.append(")");
                temp.append(REQUIRED_WHITE_SPACE);
                temp.append(selectItem.toString().split(AS)[1].replace(" ", ""));
                flagForOrderRotationWithOutSpellingMistake = true;
            }

            if(!selectItem.toString().contains(AS)){
                temp.append(OPTIONAL_WHITE_SPACE);
                temp.append("((?:");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(ALIAS) : ALIAS);
                temp.append("|");
                temp.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(AS) : AS);
                temp.append(")");
                temp.append(REQUIRED_WHITE_SPACE);
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
            buffer.append(columnNameOrder.generateRegExFor(selectedColumnNamesAsStringsWithExplicitNoneSpellingMistake));
        } else {
            buffer.append(columnNameOrder.generateRegExFor(selectedColumnNamesAsStrings));
        }

        if (plainSelect.getIntoTables() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("FROM") : "FROM");
            buffer.append(REQUIRED_WHITE_SPACE);

            List<String> selectedTableNamesAsStrings = new ArrayList<>();
            for (Table table : plainSelect.getIntoTables()) {
                selectedTableNamesAsStrings.add(table.getFullyQualifiedName());
            }
            buffer.append(tableNameOrder.generateRegExFor(selectedTableNamesAsStrings));
        }

        if (plainSelect.getFromItem() != null && plainSelect.getJoins() != null) {
            List<String> simpleJoinElements = new ArrayList<>();
            simpleJoinElements.add(plainSelect.getFromItem().toString());

            for (Join join : plainSelect.getJoins()) {
                if(join.isSimple()) simpleJoinElements.add(join.toString());
            }

            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("FROM") : "FROM");
            buffer.append(REQUIRED_WHITE_SPACE);

            if(simpleJoinElements.size() == 1){
                buffer.append(tableNameOrder.generateRegExFor(simpleJoinElements));
                for (Join join : plainSelect.getJoins()) {
                    deparseJoin(join);
                }
            } else {
                buffer.append(tableNameOrder.generateRegExFor(simpleJoinElements));
            }
        } else if (plainSelect.getFromItem() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("FROM") : "FROM");
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
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("HAVING") : "HAVING");
            buffer.append(REQUIRED_WHITE_SPACE);
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(expressionVisitor, buffer).deParse(plainSelect.isOracleSiblings(),
                    plainSelect.getOrderByElements());
        }
        if (plainSelect.isEmitChanges()){
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("EMIT") : "EMIT");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("CHANGES") : "CHANGES");
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
            buffer.append(REQUIRED_WHITE_SPACE + "FOR" + REQUIRED_WHITE_SPACE + "XML" + REQUIRED_WHITE_SPACE + "PATH(");
            buffer.append(plainSelect.getForXmlPath()).append(OPTIONAL_WHITE_SPACE + ")");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE + ")");
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
        buffer.append(subSelect.isUseBrackets() ? "(" : "");
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                withItem.accept(this);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(subSelect.isUseBrackets() ? ")" : "");
        Alias alias = subSelect.getAlias();
        String tempAlias = "(?<"+ alias +">" + alias + ")";
        subSelect.setAlias(new Alias("\\k<" + alias + ">"));
        if (alias != null) {
            buffer.append(tempAlias);
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
        buffer.append(tableName.getFullyQualifiedName());
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(alias);
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
        buffer.append(" PIVOT (").append(PlainSelect.getStringList(pivot.getFunctionItems())).append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN ").append(PlainSelect.getStringList(pivot.getInItems(), true, true)).append(")");
        if (pivot.getAlias() != null) {
            buffer.append(pivot.getAlias().toString());
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
        buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("OFFSET") : "OFFSET");
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(offset.getOffset());
        buffer.append(REQUIRED_WHITE_SPACE);
        if (offset.getOffsetParam() != null) {
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor(offset.getOffsetParam()) : offset.getOffsetParam());
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
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("OUTER") : "OUTER");
            buffer.append(REQUIRED_WHITE_SPACE);
        } else if (join.isSimple()) {
            buffer.append(",");
            buffer.append(OPTIONAL_WHITE_SPACE);
        } else {
            if (join.isRight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("RIGHT") : "RIGHT");
            } else if (join.isNatural()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("NATURAL") : "NATURAL");
            } else if (join.isFull()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("FULL") : "FULL");
            } else if (join.isLeft()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("LEFT") : "LEFT");
            } else if (join.isCross()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("CROSS") : "CROSS");
            }

            if (join.isOuter()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("OUTER") : "OUTER");
            } else if (join.isInner()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("INNER") : "INNER");
            } else if (join.isSemi()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("SEMI") : "SEMI");
            }

            if (join.isStraight()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("STRAIGHT_JOIN") : "STRAIGHT_JOIN");
                buffer.append(REQUIRED_WHITE_SPACE);
            } else if (join.isApply()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("APPLY") : "APPLY");
                buffer.append(REQUIRED_WHITE_SPACE);
            } else {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("JOIN") : "JOIN");
                buffer.append(REQUIRED_WHITE_SPACE);
            }
        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("WITHIN") : "WITHIN");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("ON") : "ON");
            buffer.append(REQUIRED_WHITE_SPACE);
            onExpression.accept(expressionVisitor);
        }
        if (!join.getUsingColumns().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("USING") : "USING");
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
            new OrderByDeParserForRegEx(expressionVisitor, buffer).deParse(list.getOrderByElements());
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
        if (withItem.isRecursive()) {
            buffer.append("RECURSIVE ");
        }
        buffer.append(withItem.getName());
        if (withItem.getWithItemList() != null) {
            buffer.append(" ").append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
        }
        buffer.append(" AS ");

        if (withItem.isUseValues()) {
            ItemsList itemsList = withItem.getItemsList();
            boolean useBracketsForValues = withItem.isUsingBracketsForValues();
            buffer.append("(VALUES ");

            ExpressionList expressionList = (ExpressionList) itemsList;
            buffer.append(
                    PlainSelect.getStringList(expressionList.getExpressions(), true, useBracketsForValues));
            buffer.append(")");
        } else {
            SubSelect subSelect = withItem.getSubSelect();
            if (!subSelect.isUseBrackets()) {
                buffer.append("(");
            }
            subSelect.accept((FromItemVisitor) this);
            if (!subSelect.isUseBrackets()) {
                buffer.append(")");
            }
        }
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
}
