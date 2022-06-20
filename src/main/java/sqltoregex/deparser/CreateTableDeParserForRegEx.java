package sqltoregex.deparser;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements an own {@link CreateTableDeParser} to generate regular expressions.
 */
public class CreateTableDeParserForRegEx extends CreateTableDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SettingsContainer settings;
    private final StatementDeParser statementDeParser;
    private final OrderRotation indexColumnNameOrder;
    private final OrderRotation columnNameOrder;
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final StringSynonymGenerator datatypeSynonymGenerator;
    private final StringSynonymGenerator otherSynonymsGenerator;

    /**
     * Short constructor for CreateTableDeParserForRegEx.
     * @param buffer {@link StringBuilder}
     * @param settingsContainer {@link SettingsContainer}
     */
    public CreateTableDeParserForRegEx(StringBuilder buffer, SettingsContainer settingsContainer) {
        this(new StatementDeParserForRegEx(buffer, settingsContainer), buffer, settingsContainer);
    }

    /**
     * Long constructor for CreateTableDeParserForRegEx.
     * @param statementDeParser {@link StatementDeParser}
     * @param buffer {@link StringBuilder}
     * @param settingsContainer {@link SettingsContainer}
     */
    public CreateTableDeParserForRegEx(StatementDeParser statementDeParser,
                                       StringBuilder buffer, SettingsContainer settingsContainer) {
        super(statementDeParser, buffer);
        this.statementDeParser = statementDeParser;
        this.settings = settingsContainer;
        this.keywordSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.tableNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.columnNameSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.indexColumnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.INDEXCOLUMNNAMEORDER);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.datatypeSynonymGenerator = settings.get(StringSynonymGenerator.class).get(SettingsOption.DATATYPESYNONYMS);
        this.otherSynonymsGenerator = settings.get(StringSynonymGenerator.class).get(SettingsOption.OTHERSYNONYMS);
    }

    /**
     * {@return list of {@literal String}s} concatenated from ColumnDefinition.
     * @param columnDefinitions List of {@link ColumnDefinition}
     */
    private List<String> columnDefinitionsListToStringList(List<ColumnDefinition> columnDefinitions){
        List<String> stringList = new LinkedList<>();

        if (columnDefinitions == null){
            return stringList;
        }

        for (ColumnDefinition definition : columnDefinitions) {
            stringList.add(deParseColumnDefinition(definition));
        }
        return stringList;
    }

    /**
     * {@return {@link Index} list concatenated to strings} because of unusable toString method for RegEx generation.
     * @param indexList List of
     */
    private List<String> indexListToStringList(List<Index> indexList){
        List<String> stringList = new LinkedList<>();

        if (indexList == null){
            return stringList;
        }

        for (Index index : indexList){
            stringList.add(deParseIndex(index));
        }
        return stringList;
    }

    /**
     * {@return {@link ForeignKeyIndex} as RegEx}.
     * Mimicking toString method of index superclass and subtracts it from the ForeignKeyIndex toString string.
     * With that its possible to deParse:
     * "CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH FULL"
     * with maximum variance in whiteSpaces.
     * @param index {@link ForeignKeyIndex}
     */
    private String deParseReferentialActions(ForeignKeyIndex index){
        String idxSpecText = PlainSelect.getStringList(index.getIndexSpec(), false, false);
        String s =  (index.getName() != null ? "CONSTRAINT " + index.getName() + " " : "")
                + index.getType() + " " + PlainSelect.getStringList(index.getColumnsNames(), true, true) + (!"".
                equals(idxSpecText) ? " " + idxSpecText : "") + " REFERENCES "  + (index).getTable() +
                PlainSelect.getStringList((index).getReferencedColumnNames(), true, true);
        return index.toString().replace(s, "").replace(" ", REQUIRED_WHITE_SPACE);
    }

    /**
     * {@return table options as {@literal String}}.
     * @param createTable {@link CreateTable}
     */
    private String deParseTableOptions(CreateTable createTable){
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

    /**
     * {@return index columns as {@literal String}}.
     * @param index {@link Index}
     */
    private String deParseIndexColumns(Index index){
        List<String> indexStringList = getStringList(index.getColumns());
        return "\\(" + OrderRotation.useOrDefault(this.indexColumnNameOrder, indexStringList)
                + "\\)";
    }


    /**
     * {@return List of {@literal String}} generated out of any list type.
     * @param list {@link List}
     */
    private List<String> getStringList(List<?> list){
        List<String> stringList = new LinkedList<>();
        list.forEach(el -> stringList.add(el.toString()));
        return stringList;
    }

    /**
     * Overrides deParse method for RegEx generation.
     * @param createTable {@link CreateTable}
     */
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

        params = deParseTableOptions(createTable);
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

    /**
     * {@return {@link ColumnDefinition} as {@literal String}}.
     * @param columnDefinition {@link ColumnDefinition}
     */
    private String deParseColumnDefinition(ColumnDefinition columnDefinition) {
        StringBuilder tmpBuffer = new StringBuilder();
        tmpBuffer.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, columnDefinition.getColumnName()));
        tmpBuffer.append(REQUIRED_WHITE_SPACE);
        List<String> synsWithSpellingMistake = new LinkedList<>();

        for (String keywordSyn : StringSynonymGenerator.generateAsListOrDefault(this.datatypeSynonymGenerator, columnDefinition.getColDataType().toString())){
            if (keywordSyn.matches(".*\\([\\w,\\s]+\\)")){
                keywordSyn = keywordSyn.replace("(", "\\(");
                keywordSyn = keywordSyn.replace(")", "\\)");
            }
            synsWithSpellingMistake.add(SpellingMistake.useOrDefault(this.keywordSpellingMistake, keywordSyn));
        }
        tmpBuffer.append(RegExGenerator.joinListToRegEx(this.datatypeSynonymGenerator, synsWithSpellingMistake));
        if (columnDefinition.getColumnSpecs() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : columnDefinition.getColumnSpecs()) {
                if (s.contains("(") && s.contains(")")){
                   s = s.replace("(", "\\(");
                   s = s.replace(")", "\\)");
                }
                stringBuilder.append(REQUIRED_WHITE_SPACE);
                stringBuilder.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, s));
            }

            String columnDefinitionString = stringBuilder.toString();
            Pattern pPrimary = Pattern.compile("(?<!UNIQUE.*)(PRIMARY)?\\s*KEY");
            Matcher mPrimary = pPrimary.matcher(columnDefinitionString);
            columnDefinitionString = mPrimary.replaceFirst("(PRIMARY\\\\s+)?KEY");

            Pattern pUnique = Pattern.compile("UNIQUE(\\s*KEY)?");
            Matcher mUnique = pUnique.matcher(columnDefinitionString);
            columnDefinitionString = mUnique.replaceFirst("UNIQUE(\\\\s+KEY)?");

            tmpBuffer.append(columnDefinitionString);
        }
        return tmpBuffer.toString();
    }

    /**
     * {@return index as {@literal String}}.
     * @param index {@link Index}
     */
    private String deParseIndex(Index index){
        StringBuilder tmpBuffer = new StringBuilder();
        if (index.getType() != null
                && (index.getType().equals("PRIMARY KEY")
                || index.getType().equals("FOREIGN KEY")
                || index.getType().equals("UNIQUE KEY")
        )
        ){
            tmpBuffer.append("(?:").append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "CONSTRAINT"));
            tmpBuffer.append(OPTIONAL_WHITE_SPACE);
            tmpBuffer.append(")?");
            //constraint name optional
            tmpBuffer.append("(?:").append(REQUIRED_WHITE_SPACE).append(".*?)");
        }

        List<String> typeSynonymList = StringSynonymGenerator.generateAsListOrDefault(this.otherSynonymsGenerator, index.getType());
        List<String> typeSynonymsWithSpellingMistake = new LinkedList<>();
        for(String typeSynonym : typeSynonymList){
            typeSynonymsWithSpellingMistake.add(SpellingMistake.useOrDefault(this.keywordSpellingMistake, typeSynonym).replace(" ", REQUIRED_WHITE_SPACE));
        }
        tmpBuffer.append(RegExGenerator.joinListToRegEx(this.otherSynonymsGenerator, typeSynonymsWithSpellingMistake));
        tmpBuffer.append(REQUIRED_WHITE_SPACE);
        //indexname optional
        tmpBuffer.append("(?:.*?").append(REQUIRED_WHITE_SPACE).append(")?");
        tmpBuffer.append(deParseIndexColumns(index));

        if (index instanceof ForeignKeyIndex foreignKeyIndex){
            tmpBuffer.append(OPTIONAL_WHITE_SPACE + "REFERENCES" + REQUIRED_WHITE_SPACE)
                    .append(foreignKeyIndex.getTable().toString())
                    .append(OPTIONAL_WHITE_SPACE)
                    .append("\\(")
                    .append(OPTIONAL_WHITE_SPACE)
                    .append(OrderRotation.useOrDefault(this.indexColumnNameOrder, foreignKeyIndex.getReferencedColumnNames()))
                    .append(OPTIONAL_WHITE_SPACE)
                    .append("\\)");
            tmpBuffer.append(deParseReferentialActions(foreignKeyIndex));
        }
        return tmpBuffer.toString();
    }
}
