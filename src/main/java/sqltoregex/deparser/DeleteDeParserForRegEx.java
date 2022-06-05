package sqltoregex.deparser;

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
    private SelectDeParserForRegEx selectDeParserForRegEx;
    private final SettingsContainer settingsContainer;
    private SpellingMistake keywordSpellingMistake;
    private SpellingMistake tableNameSpellingMistake;
    private SpellingMistake columnNameSpellingMistake;
    private OrderRotation columnNameOrderRotation;

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

        //handle table alias
        buffer.append(REQUIRED_WHITE_SPACE);
        if (delete.getTable().toString().contains(" ")){
            this.tableNameAliasMap.put(delete.getTable().toString().split(" ")[0], delete.getTable().toString().split(" ")[1]);
            this.tableNameAliasMap.put(delete.getTable().toString().split(" ")[1], delete.getTable().toString().split(" ")[0]);
            buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, delete.getTable().toString().split(" ")[0]));
            buffer.append("(");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("(?:ALIAS|AS)").append(")?");
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, delete.getTable().toString().split(" ")[1]));
        } else {
            buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, delete.getTable().toString()));
            buffer.append("(");
            buffer.append(REQUIRED_WHITE_SPACE).append("(?:ALIAS|AS)").append(".*");
            buffer.append(")?");
        }

        if (delete.getUsingList() != null && !delete.getUsingList().isEmpty()) {
            buffer.append(" USING").append(
                    delete.getUsingList().stream().map(Table::toString).collect(joining(", ", " ", "")));
        }
        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }

        if (delete.getWhere() != null) {
            buffer.append(" WHERE ");
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
            buffer.append(" RETURNING ").append(PlainSelect.
                    getStringList(delete.getReturningExpressionList(), true, false));
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
