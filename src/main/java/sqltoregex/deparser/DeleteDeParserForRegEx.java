package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

import static java.util.stream.Collectors.joining;

/**
 * implements own delete statement deparser for regular expressions
 */
public class DeleteDeParserForRegEx extends DeleteDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    Map<String, String> tableNameAliasMap = new HashMap<>();
    private ExpressionDeParserForRegEx expressionDeParserForRegEx;
    private final SelectDeParserForRegEx selectDeParserForRegEx;
    private final SettingsContainer settingsContainer;
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final OrderRotation columnNameOrderRotation;
    private final OrderRotation tableNameOrderRotation;

    /**
     * default constructor
     * @param settingsContainer SettingsContainer
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer) {
        this(settingsContainer, new ExpressionDeParserForRegEx(settingsContainer), new StringBuilder());
    }

    /**
     * explicit constructor with specific ExpressionDeParserForRegEx and StringBuilder
     * @param settingsContainer SettingsContainer
     * @param expressionDeParserForRegEx ExpressionDeParserForRegEx
     * @param buffer StringBuilder
     */
    public DeleteDeParserForRegEx(SettingsContainer settingsContainer, ExpressionDeParserForRegEx expressionDeParserForRegEx, StringBuilder buffer) {
        super(expressionDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.settingsContainer = settingsContainer;
        this.keywordSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.tableNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.columnNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.columnNameOrderRotation = settingsContainer.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.tableNameOrderRotation = settingsContainer.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER);
        this.selectDeParserForRegEx = new SelectDeParserForRegEx(settingsContainer);
    }

    /**
     * overrides deparse method for implement regular expressions while deparsing the update statement
     * @param delete Delete
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", "PMD.NPathComplexity"})
    public void deParse(Delete delete) {
        if (delete.getWithItemsList() != null && !delete.getWithItemsList().isEmpty()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "WITH"));
            buffer.append(REQUIRED_WHITE_SPACE);
            this.buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(delete));
        }
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "DELETE"));

        if (delete.getModifierPriority() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, String.valueOf(delete.getModifierPriority())));
        }

        if (delete.isModifierQuick()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "QUICK"));
        }

        if (delete.isModifierIgnore()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "IGNORE"));
        }

        //DELETE FROM == DELETE * FROM
        if (null == delete.getTables()) {
            buffer.append(REQUIRED_WHITE_SPACE).append("\\*").append(REQUIRED_WHITE_SPACE);
        }

        List<String> tableList = new LinkedList<>();
        //DELETE tab == DELETE tab.*, DELETE tab.col, DELETE tab||tab.col (AS|ALIAS) xyz
        if (delete.getTables() != null && !delete.getTables().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            List<Table> unEditedTableList = delete.getTables();

            for(Table table : unEditedTableList){
                StringBuilder temp = new StringBuilder();

                //handle optional star-operator and table/column-name spelling
                if(table.getFullyQualifiedName().contains(".")){
                    temp.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, table.getFullyQualifiedName().split("\\.")[0]));
                    temp.append("\\.");
                    temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, table.getFullyQualifiedName().split("\\.")[1]));
                } else {
                    temp.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, table.getFullyQualifiedName()));
                    temp.append("(\\.\\*)?");
                }

                //handle alias
                if(null != table.getAlias()){
                    temp.append(REQUIRED_WHITE_SPACE).append("(").append("(?:ALIAS|AS)").append(REQUIRED_WHITE_SPACE).append(")?").append(table.getAlias().toString().replace("AS", "").replace(" ", ""));
                } else {
                    temp.append("(").append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(".*").append(")?");
                }
                tableList.add(temp.toString());
            }

            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrderRotation, tableList));
        }

        if (delete.getOutputClause()!=null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "OUTPUT"));
            buffer.append(REQUIRED_WHITE_SPACE);
            List<String> outputClauses = new ArrayList<>();
            for(SelectItem selectItem : delete.getOutputClause().getSelectItemList()){
                outputClauses.add(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, selectItem.toString()));
            }
            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrderRotation, outputClauses));
        }

        if (delete.isHasFrom()) {
            buffer.append(REQUIRED_WHITE_SPACE).append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "FROM"));
        }

        List<Object> allTablesWithJoin = new LinkedList<>();
        allTablesWithJoin.add(delete.getTable());
        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                if (join.isSimple()) {
                    allTablesWithJoin.add(join);
                }
            }
        }
        //handle table alias
        buffer.append(REQUIRED_WHITE_SPACE);
        List<String> toRotateTables = new LinkedList<>();
        for(Object o : allTablesWithJoin){
            StringBuilder tmpbuffer = new StringBuilder();
            if (o.toString().contains(" ")){
                this.tableNameAliasMap.put(o.toString().split(" ")[0], o.toString().split(" ")[1]);
                this.tableNameAliasMap.put(o.toString().split(" ")[1], o.toString().split(" ")[0]);
                tmpbuffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, o.toString().split(" ")[0]));
                tmpbuffer.append("(");
                tmpbuffer.append(REQUIRED_WHITE_SPACE);
                tmpbuffer.append("(?:ALIAS|AS)").append(")?");
                tmpbuffer.append(REQUIRED_WHITE_SPACE);
                tmpbuffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, o.toString().split(" ")[1]));
            } else {
                tmpbuffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, o.toString()));
                tmpbuffer.append("(");
                tmpbuffer.append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(".*");
                tmpbuffer.append(")?");
            }
            toRotateTables.add(tmpbuffer.toString());
        }
        buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrderRotation, toRotateTables));


        if (delete.getUsingList() != null && !delete.getUsingList().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "USING"));
            buffer.append(REQUIRED_WHITE_SPACE);
            List<String> tableNameListAsStrings = new LinkedList<>();
            for(Table table : delete.getUsingList()){
                tableNameListAsStrings.add(table.toString());
            }
            buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrderRotation, tableNameListAsStrings));
        }


        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                if (!join.isSimple()) {
                    SelectDeParserForRegEx joinSelectDeParserForRegEx = new SelectDeParserForRegEx(settingsContainer);
                    joinSelectDeParserForRegEx.setBuffer(buffer);
                    joinSelectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
                    joinSelectDeParserForRegEx.deparseJoin(join);
                }
            }
        }

        if (delete.getWhere() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "WHERE")).append(REQUIRED_WHITE_SPACE);
            delete.getWhere().accept(this.getExpressionDeParserForRegEx());
        }

        if (delete.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(
                    this.getExpressionDeParserForRegEx(),
                    buffer,
                    this.getSettingsContainer()
            ).deParse(delete.getOrderByElements());
        }
        if (delete.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, settingsContainer).deParse(delete.getLimit());
        }

        if (delete.getReturningExpressionList() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "RETURNING")).append(REQUIRED_WHITE_SPACE);
            List<String> returningExpressions = new LinkedList<>();
            for(SelectItem selectItem : delete.getReturningExpressionList()){
                returningExpressions.add(selectItem.toString());
            }
            buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrderRotation, returningExpressions));
        }
    }

    /**
     * get ExpressionDeParserForRegEx
     * @return ExpressionDeParserForRegEx
     */
    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    /**
     * set ExpressionDeParserForRegEx
     * @param expressionDeParserForRegEx ExpressionDeParserForRegEx
     */
    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    /**
     * get private final settings container, init while construct the object
     * @return SettingsContainer
     */
    private SettingsContainer getSettingsContainer(){
        return this.settingsContainer;
    }

    /**
     * actual sample of collected tablenames and alias names
     * @return Map with tablename ↔ alias & alias ↔ tablename
     */
    private Map<String, String> getTableNameAliasMap() {
        return this.tableNameAliasMap;
    }
}
