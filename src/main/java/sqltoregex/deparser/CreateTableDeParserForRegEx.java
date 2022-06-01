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

import java.util.*;

public class CreateTableDeParserForRegEx extends CreateTableDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";

    private final SettingsContainer settings;
    private StatementDeParser statementDeParser;
    private final OrderRotation indexColumnNameOrder;
    private final OrderRotation columnNameOrder;

    public CreateTableDeParserForRegEx(StringBuilder buffer, SettingsContainer settingsContainer) {
        this(new StatementDeParserForRegEx(buffer, settingsContainer), buffer, settingsContainer);
    }

    public CreateTableDeParserForRegEx(StatementDeParser statementDeParser,
                                       StringBuilder buffer, SettingsContainer settingsContainer) {
        super(statementDeParser, buffer);
        this.statementDeParser = statementDeParser;
        this.settings = settingsContainer;
        this.indexColumnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.INDEXCOLUMNNAMEORDER);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
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
                tmpBuffer.append("(?:CONSTRAINT").append(REQUIRED_WHITE_SPACE);
                tmpBuffer.append(index.getName()).append(REQUIRED_WHITE_SPACE);
                tmpBuffer.append(index.getType()).append(OPTIONAL_WHITE_SPACE);
                tmpBuffer.append(concatIndexColumns(index));
                tmpBuffer.append("|");
            }

            tmpBuffer.append(index.getType());
            tmpBuffer.append(!index.getName().isEmpty() ? REQUIRED_WHITE_SPACE + index.getName() : "");
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
                tmpBuffer.append(RegExGenerator.useOrderRotation(this.indexColumnNameOrder, columns));
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
        return "\\(" + RegExGenerator.useOrderRotation(this.indexColumnNameOrder, indexStringList)
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
        buffer.append("CREATE").append(REQUIRED_WHITE_SPACE);
        if (createTable.isOrReplace()) {
            buffer.append("OR").append(REQUIRED_WHITE_SPACE).append("REPLACE").append(REQUIRED_WHITE_SPACE);
        }
        if (createTable.isUnlogged()) {
            buffer.append("UNLOGGED").append(REQUIRED_WHITE_SPACE);
        }
        String params = PlainSelect.getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append("TABLE").append(REQUIRED_WHITE_SPACE);
        if (createTable.isIfNotExists()) {
            buffer.append("IF").append(REQUIRED_WHITE_SPACE)
                    .append("NOT").append(REQUIRED_WHITE_SPACE)
                    .append("EXISTS").append(REQUIRED_WHITE_SPACE);
        }
        buffer.append(createTable.getTable().getFullyQualifiedName());

        if (createTable.getColumns() != null && !createTable.getColumns().isEmpty()) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");

            buffer.append(settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER).generateRegExFor(createTable.getColumns()));
            buffer.append("\\)");
        }
        if (createTable.getColumnDefinitions() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\(");

            List<String> colAndIndexList = new LinkedList<>();

            colAndIndexList.addAll(columnDefinitionsListToStringList(createTable.getColumnDefinitions()));
            colAndIndexList.addAll(indexListToStringList(createTable.getIndexes()));


            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrder , colAndIndexList));

            buffer.append("\\)");
        }

        params = getProcessedTableOptions(createTable);
        if (!"".equals(params)) {
            buffer.append(REQUIRED_WHITE_SPACE).append(params);
        }

        //ORACLE: ALTER TABLE hr.employees ENABLE ROW MOVEMENT
        if (createTable.getRowMovement() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(createTable.getRowMovement().getMode().toString()).append(REQUIRED_WHITE_SPACE).append("ROW").append("MOVEMENT");
        }
        if (createTable.getSelect() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append("AS").append(REQUIRED_WHITE_SPACE);
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
            buffer.append(REQUIRED_WHITE_SPACE).append("LIKE").append(REQUIRED_WHITE_SPACE);
            Table table = createTable.getLikeTable();
            buffer.append(table.getFullyQualifiedName());
            buffer.append("(?:").append(OPTIONAL_WHITE_SPACE).append("\\))?");
        }
    }

    private void concatColumnDefinition(ColumnDefinition columnDefinition, StringBuilder buffer) {
        buffer.append(columnDefinition.getColumnName());
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(columnDefinition.getColDataType().toString());
        if (columnDefinition.getColumnSpecs() != null) {
            for (String s : columnDefinition.getColumnSpecs()) {
                buffer.append(REQUIRED_WHITE_SPACE);
                buffer.append(s);
            }
        }
    }
}