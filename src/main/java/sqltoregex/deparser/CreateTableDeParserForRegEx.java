package sqltoregex.deparser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.CreateTableDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.*;

/**
 * Implements an own create table statement deparser to generate regular expressions.
 */
public class CreateTableDeParserForRegEx extends CreateTableDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SettingsContainer settings;
    private final StatementDeParser statementDeParser;
    private final OrderRotation indexColumnNameOrder;
    private final OrderRotation columnNameOrder;
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake indexColumnNameSpelling;
    private final SpellingMistake tableNameSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final StringSynonymGenerator datatypeSynonymGenerator;

    /**
     * Short constrcutor for CreateTableDeParserForRegEx.
     * @param buffer {@link StringBuilder}
     * @param settingsContainer
     */
    public CreateTableDeParserForRegEx(StringBuilder buffer, SettingsContainer settingsContainer) {
        this(new StatementDeParserForRegEx(buffer, settingsContainer), buffer, settingsContainer);
    }

    public CreateTableDeParserForRegEx(StatementDeParser statementDeParser,
                                       StringBuilder buffer, SettingsContainer settingsContainer) {
        super(statementDeParser, buffer);
        this.statementDeParser = statementDeParser;
        this.settings = settingsContainer;
        this.keywordSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.indexColumnNameSpelling = settingsContainer.get(SpellingMistake.class).get(SettingsOption.INDEXCOLUMNNAMESPELLING);
        this.tableNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.columnNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.indexColumnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.INDEXCOLUMNNAMEORDER);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.datatypeSynonymGenerator = settings.get(StringSynonymGenerator.class).get(SettingsOption.DATATYPESYNONYMS);
    }

    private List<String> columnDefinitionsListToStringList(List<ColumnDefinition> columnDefinitions){
        List<String> stringList = new LinkedList<>();

        if (columnDefinitions == null){
            return stringList;
        }

        for (ColumnDefinition definition : columnDefinitions) {
            StringBuilder tmpBuffer = new StringBuilder();
            concatColumnDefinition(definition, tmpBuffer);
            stringList.add(tmpBuffer.toString());
        }
        return stringList;
    }

    private List<String> indexListToStringList(List<Index> indexList){
        List<String> stringList = new LinkedList<>();

        if (indexList == null){
            return stringList;
        }

        for (Index index : indexList){
            StringBuilder tmpBuffer = new StringBuilder();
            if (index.getType() != null && (index.getType().equals("PRIMARY KEY") || index.getType().equals("FOREIGN KEY"))){
                tmpBuffer.append("(?:").append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "CONSTRAINT")).append(REQUIRED_WHITE_SPACE);
                tmpBuffer.append(SpellingMistake.useOrDefault(this.indexColumnNameSpelling, index.getName())).append(REQUIRED_WHITE_SPACE);
                tmpBuffer.append(SpellingMistake.useOrDefault(this.indexColumnNameSpelling, index.getType())).append(OPTIONAL_WHITE_SPACE);
                tmpBuffer.append(concatIndexColumns(index));
                tmpBuffer.append("|");
            }

            tmpBuffer.append(index.getType());
            tmpBuffer.append(!index.getName().isEmpty() ? REQUIRED_WHITE_SPACE + SpellingMistake.useOrDefault(this.indexColumnNameSpelling, index.getName()) : "");
            tmpBuffer.append(REQUIRED_WHITE_SPACE);
            tmpBuffer.append(concatIndexColumns(index));
            if (index.getType() != null && (index.getType().equals("PRIMARY KEY") || index.getType().equals("FOREIGN KEY"))){
                tmpBuffer.append(")");
            }
            stringList.add(tmpBuffer.toString());
        }
        return stringList;
    }

    private String getProcessedTableOptions(CreateTable createTable){
        StringBuilder tmpBuffer = new StringBuilder();
        if (createTable.getTableOptionsStrings() == null){
            return tmpBuffer.toString();
        }

        Iterator<String> iterator = createTable.getTableOptionsStrings().iterator();

        while (iterator.hasNext()){
            String tableOption = iterator.next();
            if (tableOption.contains("(")){
                tmpBuffer.append(tableOption, 0, tableOption.indexOf('('));
                tmpBuffer.append("\\(");
                String columnsString = tableOption.substring(tableOption.indexOf('(') + 1, tableOption.lastIndexOf(')'));
                List<String> columns = new LinkedList<>(List.of(columnsString.split(",")));
                tmpBuffer.append(OrderRotation.useOrDefault(this.indexColumnNameOrder, columns));
                tmpBuffer.append("\\)");
            }else if (tableOption.equals("=")) {
                tmpBuffer.delete(tmpBuffer.length() - REQUIRED_WHITE_SPACE.length(), tmpBuffer.length());
                tmpBuffer.append("(?:")
                        .append(OPTIONAL_WHITE_SPACE).append("=").append(OPTIONAL_WHITE_SPACE)
                        .append("|")
                        .append(REQUIRED_WHITE_SPACE)
                        .append(")");
            } else {
                tmpBuffer.append(tableOption);
                if (iterator.hasNext()) {
                    tmpBuffer.append(REQUIRED_WHITE_SPACE);
                } else {
                    tmpBuffer.append(OPTIONAL_WHITE_SPACE);
                }
            }

        }
        return tmpBuffer.toString();
    }

    private String concatIndexColumns(Index index){
        List<String> indexStringList = getStringList(index.getColumns());
        return "\\(" + OrderRotation.useOrDefault(this.indexColumnNameOrder, indexStringList)
                + "\\)";
    }

    private List<String> getStringList(List<?> list){
        List<String> stringList = new LinkedList<>();
        list.forEach(el -> stringList.add(el.toString()));
        return stringList;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(CreateTable createTable) {
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "CREATE")).append(REQUIRED_WHITE_SPACE);
        if (createTable.isOrReplace()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "OR"))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "REPLACE"))
                    .append(REQUIRED_WHITE_SPACE);
        }
        if (createTable.isUnlogged()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "UNLOGGED")).append(REQUIRED_WHITE_SPACE);
        }
        String params = PlainSelect.getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "TABLE")).append(REQUIRED_WHITE_SPACE);
        if (createTable.isIfNotExists()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "IF")).append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "NOT")).append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "EXISTS")).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, createTable.getTable().getFullyQualifiedName()));

        if (createTable.getColumns() != null && !createTable.getColumns().isEmpty()) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");
            buffer.append(settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER)
                    .generateRegExFor(createTable.getColumns().stream().map(col -> SpellingMistake.useOrDefault(this.columnNameSpellingMistake, col)).toList()));
            buffer.append("\\)");
        }
        if (createTable.getColumnDefinitions() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");

            List<String> colAndIndexList = new LinkedList<>();

            colAndIndexList.addAll(columnDefinitionsListToStringList(createTable.getColumnDefinitions()));
            colAndIndexList.addAll(indexListToStringList(createTable.getIndexes()));

            buffer.append(OrderRotation.useOrDefault(this.columnNameOrder , colAndIndexList));

            buffer.append("\\)");
        }

        params = getProcessedTableOptions(createTable);
        if (!"".equals(params)) {
            buffer.append(REQUIRED_WHITE_SPACE).append(params);
        }

        //ORACLE: ALTER TABLE hr.employees ENABLE ROW MOVEMENT
        if (createTable.getRowMovement() != null) {
            buffer.append(REQUIRED_WHITE_SPACE)
                    .append(createTable.getRowMovement().getMode().toString())
                    .append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ROW"))
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "MOVEMENT"));
        }
        if (createTable.getSelect() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "AS")).append(REQUIRED_WHITE_SPACE);
            if (createTable.isSelectParenthesis()) {
                buffer.append("\\(");
            }
            Select sel = createTable.getSelect();
            sel.accept(this.statementDeParser);
            if (createTable.isSelectParenthesis()) {
                buffer.append("\\)");
            }
        }
        if (createTable.getLikeTable() != null) {
            buffer.append("(?:\\(").append(OPTIONAL_WHITE_SPACE).append(")?");
            buffer.append(REQUIRED_WHITE_SPACE).append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "LIKE")).append(REQUIRED_WHITE_SPACE);
            Table table = createTable.getLikeTable();
            buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, table.getFullyQualifiedName()));
            buffer.append("(?:").append(OPTIONAL_WHITE_SPACE).append("\\))?");
        }
    }

    private void concatColumnDefinition(ColumnDefinition columnDefinition, StringBuilder buffer) {
        buffer.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, columnDefinition.getColumnName()));
        buffer.append(REQUIRED_WHITE_SPACE);
        List<String> synsWithSpellingMistake = new LinkedList<>();

        for (String keywordSyn : StringSynonymGenerator.generateAsListOrDefault(this.datatypeSynonymGenerator, columnDefinition.getColDataType().toString())){
            synsWithSpellingMistake.add(SpellingMistake.useOrDefault(this.keywordSpellingMistake, keywordSyn));
        }
        buffer.append(RegExGenerator.joinListToRegEx(this.datatypeSynonymGenerator, synsWithSpellingMistake));
        if (columnDefinition.getColumnSpecs() != null) {
            for (String s : columnDefinition.getColumnSpecs()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, s));
            }
        }
    }
}
