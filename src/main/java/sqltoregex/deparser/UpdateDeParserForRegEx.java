package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

public class UpdateDeParserForRegEx extends UpdateDeParser {
    private final Map<String, String> tableNameAliasCombinations = new HashMap<>();
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final OrderRotation columnNameOrderRotation;
    private final OrderRotation tableNameOrderRotation;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    private final SettingsContainer settingsContainer;

    public UpdateDeParserForRegEx(SettingsContainer settingsContainer, StringBuilder buffer) {
        this(new ExpressionDeParserForRegEx(settingsContainer), new SelectDeParserForRegEx(settingsContainer), buffer, settingsContainer);
    }

    public UpdateDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsContainer settingsManager) {
        super(expressionDeParserForRegEx, buffer);
        this.settingsContainer = settingsManager;
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
        this.keywordSpellingMistake = settingsManager.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameSpellingMistake = settingsManager.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.tableNameSpellingMistake = settingsManager.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.columnNameOrderRotation = settingsManager.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.tableNameOrderRotation = settingsManager.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    public void deParse(Update update) {
        if (update.getWithItemsList() != null && !update.getWithItemsList().isEmpty()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "WITH", true);
            this.buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(update));
        }

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "UPDATE", true);

        if (update.getModifierPriority() != null) {
            buffer.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, update.getModifierPriority().toString())).append(REQUIRED_WHITE_SPACE);
        }
        if (update.isModifierIgnore()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "IGNORE", true);
        }

        List<String> tableList = new ArrayList<>();
        if(update.getTable().toString().contains(" ")){
            tableList.add(this.extractTableNameAlias(update.getTable().toString()));
        } else tableList.add(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, update.getTable().toString()));

        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                if (join.isSimple()) {
                    tableList.add(extractTableNameAlias(join.toString()));
                }
            }
        }

        buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrderRotation, tableList));

        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                if (!join.isSimple()) {
                    buffer.append(REQUIRED_WHITE_SPACE);
                    this.selectDeParserForRegEx.deparseJoin(join);
                }
            }
        }

        this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "SET", true);

        List<String> insertedSet = new ArrayList<>();
        for(UpdateSet updateSet : update.getUpdateSets()){
            StringBuilder singleSet = new StringBuilder();
            singleSet.append(OPTIONAL_WHITE_SPACE + "\\(?" + OPTIONAL_WHITE_SPACE);
            Iterator<Column> columnIterator = updateSet.getColumns().iterator();
            while(columnIterator.hasNext()){
                singleSet.append(this.checkOfExistingTableNameAndAlias(columnIterator.next().toString()));
                if(columnIterator.hasNext()) singleSet.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            singleSet.append(OPTIONAL_WHITE_SPACE + "\\)?" + OPTIONAL_WHITE_SPACE);

            singleSet.append(OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);

            singleSet.append(OPTIONAL_WHITE_SPACE + "\\(?" + OPTIONAL_WHITE_SPACE);
            Iterator<Expression> expressionIterator = updateSet.getExpressions().iterator();
            while(expressionIterator.hasNext()){
                singleSet.append(expressionIterator.next().toString());
                if(expressionIterator.hasNext()) singleSet.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            singleSet.append(OPTIONAL_WHITE_SPACE + "\\)?" + OPTIONAL_WHITE_SPACE);

            insertedSet.add(singleSet.toString());
        }
        buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrderRotation, insertedSet));

        if (update.getOutputClause() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "OUTPUT", true);
            List<String> outputClauses = new ArrayList<>();
            for(SelectItem selectItem : update.getOutputClause().getSelectItemList()){
                outputClauses.add(selectItem.toString());
            }
            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrderRotation, outputClauses));
        }

        if (update.getFromItem() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "FROM", true);
            List<String> tableListFromItem = new ArrayList<>();
            if(update.getFromItem().toString().contains(" ")){
                tableListFromItem.add(this.extractTableNameAlias(update.getFromItem().toString()));
            } else tableListFromItem.add(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, update.getFromItem().toString()));

            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    if (join.isSimple()) {
                        tableListFromItem.add(extractTableNameAlias(join.toString()));
                    }
                }
            }

            buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrderRotation, tableListFromItem));

            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    if (!join.isSimple()) {
                        buffer.append(REQUIRED_WHITE_SPACE);
                        this.selectDeParserForRegEx.deparseJoin(join);
                    }
                }
            }
        }

        if (update.getWhere() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "WHERE", true);
            update.getWhere().accept(this.getExpressionDeParserForRegEx());
        }
        if (update.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(this.getExpressionDeParserForRegEx(), buffer, this.settingsContainer).deParse(update.getOrderByElements(), update.getFromItem());
        }
        if (update.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, this.settingsContainer).deParse(update.getLimit());
        }

        if (update.getReturningExpressionList() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "RETURNING", true);
            List<String> returningExpressionList = new ArrayList<>();
            for(SelectItem selectItem : update.getReturningExpressionList()){
                returningExpressionList.add(selectItem.toString());
            }
            buffer.append(RegExGenerator.useOrderRotation(this.columnNameOrderRotation, returningExpressionList));
        }
    }

    public ExpressionDeParserForRegEx getExpressionDeParserForRegEx() {
        return this.expressionDeParserForRegEx;
    }

    public void setExpressionDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx) {
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
    }

    @Override
    public void visit(OrderByElement orderBy) {
        throw new UnsupportedOperationException();
    }

    private void setKeywordSpellingMistakeWithRequiredWhitespaces(boolean whiteSpaceBefore, String keyword, boolean whiteSpaceAfter){
        buffer.append(whiteSpaceBefore ? REQUIRED_WHITE_SPACE : "");
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, keyword));
        buffer.append(whiteSpaceAfter ? REQUIRED_WHITE_SPACE : "");
    }

    private String extractTableNameAlias(String columnName){
        StringBuilder temp = new StringBuilder();
        if (columnName.contains(" ")){
            String fullName = columnName.split(" ")[0];
            String alias = columnName.split(" ")[1];
            this.tableNameAliasCombinations.put(fullName, alias);
            this.tableNameAliasCombinations.put(alias, fullName);
            temp.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, fullName));
            temp.append("(" + REQUIRED_WHITE_SPACE + "(?:")
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "ALIAS"))
                    .append("|")
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "AS"))
                    .append(")")
                    .append(")?")
                    .append(REQUIRED_WHITE_SPACE);
            temp.append(alias);
            return temp.toString();
        } else {
            String fullName = columnName.split(" ")[0];
            temp.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, fullName));
            temp.append("(" + REQUIRED_WHITE_SPACE + "(?:")
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "ALIAS"))
                    .append("|")
                    .append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "AS"))
                    .append(")?")
                    .append(".*")
                    .append(")?");
            return temp.toString();
        }
    }

    private String checkOfExistingTableNameAndAlias(String column){
        StringBuilder temp = new StringBuilder();
        if(column.contains(".")){
            String tab = column.split("\\.")[0];
            String col = column.split("\\.")[1];
            temp.append("(?:")
                    .append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, tab))
                    .append("|")
                    .append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, this.tableNameAliasCombinations.getOrDefault(tab, tab)))
                    .append(")\\.");
            temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, col));
        } else{
            temp.append(RegExGenerator.useSpellingMistake(this.columnNameSpellingMistake, column));
        }
        return temp.toString();
    }
}
