package sqltoregex.deparser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

/**
 * implements own delete statement deparser for regular expressions
 */
public class DeleteDeParserForRegEx extends DeleteDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
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
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "WITH"));
            buffer.append(REQUIRED_WHITE_SPACE);
            this.buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(delete));
        }
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "DELETE"));

        if (delete.getModifierPriority() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, String.valueOf(delete.getModifierPriority())));
        }

        if (delete.isModifierQuick()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "QUICK"));
        }

        if (delete.isModifierIgnore()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "IGNORE"));
        }

        if (null == delete.getTables()) {
            buffer.append(REQUIRED_WHITE_SPACE).append("\\*").append(REQUIRED_WHITE_SPACE);
        }

        List<String> tableList = new LinkedList<>();
        if (delete.getTables() != null && !delete.getTables().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            List<Table> unEditedTableList = delete.getTables();

            for(Table table : unEditedTableList){
                StringBuilder temp = new StringBuilder();
                if(table.getFullyQualifiedName().contains(".")){
                    temp.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, table.getFullyQualifiedName().split("\\.")[0]));
                    temp.append("\\.");
                    temp.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, table.getFullyQualifiedName().split("\\.")[1]));
                } else {
                    temp.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, table.getFullyQualifiedName()));
                    temp.append("(\\.\\*)?");
                }

                if(null != table.getAlias()){
                    temp.append(REQUIRED_WHITE_SPACE).append("(").append("(?:ALIAS|AS)").append(REQUIRED_WHITE_SPACE).append(")?").append(table.getAlias().toString().replace("AS", "").replace(" ", ""));
                } else {
                    temp.append("(").append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(".*").append(")?");
                }
                tableList.add(temp.toString());
            }

            buffer.append(OrderRotation.useOrDefault(this.columnNameOrderRotation, tableList));
        }

        if (delete.getOutputClause()!=null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "OUTPUT")).append(REQUIRED_WHITE_SPACE);
            List<String> outputClauses = new ArrayList<>();
            for(SelectItem selectItem : delete.getOutputClause().getSelectItemList()){
                outputClauses.add(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, selectItem.toString()));
            }
            buffer.append(OrderRotation.useOrDefault(this.columnNameOrderRotation, outputClauses));
        }

        if (delete.isHasFrom()) {
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "FROM"));
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
                this.expressionDeParserForRegEx.addTableNameAlias(o.toString());
                tmpbuffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, o.toString().split(" ")[0]));
                tmpbuffer.append("(").append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(")?").append(REQUIRED_WHITE_SPACE);
                tmpbuffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, o.toString().split(" ")[1]));
            } else {
                tmpbuffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, o.toString()));
                tmpbuffer.append("(").append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(".*").append(")?");
            }
            toRotateTables.add(tmpbuffer.toString());
        }
        buffer.append(OrderRotation.useOrDefault(this.tableNameOrderRotation, toRotateTables));


        if (delete.getUsingList() != null && !delete.getUsingList().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "USING")).append(REQUIRED_WHITE_SPACE);
            List<String> tableNameListAsStrings = new LinkedList<>();
            for(Table table : delete.getUsingList()){
                tableNameListAsStrings.add(table.toString());
            }
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrderRotation, tableNameListAsStrings));
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
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "WHERE")).append(REQUIRED_WHITE_SPACE);
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
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "RETURNING")).append(REQUIRED_WHITE_SPACE);
            List<String> returningExpressions = new LinkedList<>();
            for(SelectItem selectItem : delete.getReturningExpressionList()){
                returningExpressions.add(selectItem.toString());
            }
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrderRotation, returningExpressions));
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
}
