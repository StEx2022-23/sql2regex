package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

/**
 * Implements own {@link InsertDeParser} to generate regex.
 */
public class InsertDeParserForRegEx extends InsertDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final SpellingMistake columnNameSpellingMistake;
    private final OrderRotation columnNameOrder;
    private final OrderRotation tableNameOrder;
    private final OrderRotation insertIntoValuesOrder;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    SettingsContainer settings;

    /**
     * Short constructor for InsertDeParserForRegEx. Init the expanded constructor.
     * @param settings {@link SettingsContainer}
     */
    public InsertDeParserForRegEx(SettingsContainer settings) {
        this(new ExpressionDeParserForRegEx(settings), new SelectDeParserForRegEx(settings), new StringBuilder(), settings);
    }

    /**
     * Extended constructor for InsertDeParserForRegEx.
     * @param expressionDeParserForRegEx {@link ExpressionDeParserForRegEx}
     * @param selectDeParserForRegEx {@link SelectDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public InsertDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsContainer settings) {
        super(expressionDeParserForRegEx, selectDeParserForRegEx, buffer);
        this.settings = settings;
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.columnNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.tableNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.tableNameOrder = settings.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER);
        this.columnNameOrder = settings.get(OrderRotation.class).get(SettingsOption.COLUMNNAMEORDER);
        this.insertIntoValuesOrder = settings.get(OrderRotation.class).get(SettingsOption.INSERTINTOVALUESORDER);
    }

    /**
     * Deparses the whole {@link Insert} object.
     * {@link SuppressWarnings}: PMD.CyclomaticComplexity, PMD.ExcessiveMethodLength and PMD.NPathComplexity
     * @param insert {@link Insert}
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void deParse(Insert insert) {
        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "WITH"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(insert));
        }

        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "INSERT")).append(REQUIRED_WHITE_SPACE);
        if (insert.getModifierPriority() != null) {
            buffer.append(insert.getModifierPriority()).append(REQUIRED_WHITE_SPACE);
        }
        if (insert.isModifierIgnore()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "IGNORE")).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "INTO")).append(REQUIRED_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, insert.getTable().toString())).append(REQUIRED_WHITE_SPACE);

        if (insert.getColumns() != null && !(insert.getItemsList(ExpressionList.class).getExpressions().get(0) instanceof RowConstructor)) {
            Map<String, String> mappedColumnsAndRelatedValues = new LinkedHashMap<>();
            buffer.append("(?:");
            Iterator<String> columnsOrderOptionsIterator = Arrays.stream(this.generateListOfColumnsOrderOption(mappedColumnsAndRelatedValues, insert)).iterator();
            while (columnsOrderOptionsIterator.hasNext()) {
                Iterator<String> extractedColumnsIterator = Arrays.stream(this.generateColumnArray(columnsOrderOptionsIterator)).iterator();
                buffer.append("\\(");
                buffer.append(OPTIONAL_WHITE_SPACE);
                while (extractedColumnsIterator.hasNext()) {
                    buffer.append(this.selectDeParserForRegEx.generateRegExForQuotationMarks()).append("?");
                    buffer.append(
                            SpellingMistake.useOrDefault(
                                    this.tableNameSpellingMistake,
                                    mappedColumnsAndRelatedValues.get(extractedColumnsIterator.next())
                            ).replaceAll(this.selectDeParserForRegEx.generateRegExForQuotationMarks(), "")
                    );
                    buffer.append(this.selectDeParserForRegEx.generateRegExForQuotationMarks()).append("?");
                    if (extractedColumnsIterator.hasNext()) {
                        buffer.append(OPTIONAL_WHITE_SPACE);
                        buffer.append(",");
                        buffer.append(OPTIONAL_WHITE_SPACE);
                    }
                }
                buffer.append(OPTIONAL_WHITE_SPACE);
                buffer.append("\\)");
                buffer.append(columnsOrderOptionsIterator.hasNext() ? "|" : "");
            }
            buffer.append(")");
        } else if (insert.getColumns() != null && insert.getItemsList(ExpressionList.class).getExpressions().get(0) instanceof RowConstructor) {
            Map<String, List<String>> mappedColumnsAndRelatedValues = new LinkedHashMap<>();
            buffer.append("(?:");
            Iterator<String> columnsOrderOptionsIterator = Arrays.stream(this.generateListOfColumnsOrderOptionForMultipleValues(mappedColumnsAndRelatedValues, insert)).iterator();
            while (columnsOrderOptionsIterator.hasNext()) {
                Iterator<String> extractedColumnsIterator = Arrays.stream(this.generateColumnArray(columnsOrderOptionsIterator)).iterator();
                List<String> tempValuesRelatedToActualCol = new ArrayList<>();
                int valueDepth = 0;
                while (extractedColumnsIterator.hasNext()) {
                    String tempCol = extractedColumnsIterator.next();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(tempCol).append(",");
                    for (String str : mappedColumnsAndRelatedValues.get(tempCol)) {
                        stringBuilder.append(str).append(",");
                    }
                    stringBuilder.replace(stringBuilder.toString().length() - 1, stringBuilder.toString().length(), "");
                    tempValuesRelatedToActualCol.add(stringBuilder.toString());
                    valueDepth = mappedColumnsAndRelatedValues.get(tempCol).size();
                }

                List<String> valueListInRightOrder = new ArrayList<>();
                for (int i = 0; i < valueDepth; i++) {
                    StringBuilder temp = new StringBuilder();
                    temp.append("\\(").append(OPTIONAL_WHITE_SPACE);
                    Iterator<String> stringIterator = tempValuesRelatedToActualCol.iterator();
                    while (stringIterator.hasNext()) {
                        temp.append(this.selectDeParserForRegEx.generateRegExForQuotationMarks()).append("?");
                        temp.append(
                                SpellingMistake.useOrDefault(
                                        this.tableNameSpellingMistake,
                                        stringIterator.next().split(",")[i + 1]
                                ).replaceAll(this.selectDeParserForRegEx.generateRegExForQuotationMarks(), ""));
                        temp.append(this.selectDeParserForRegEx.generateRegExForQuotationMarks()).append("?");
                        if (stringIterator.hasNext()) temp.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
                    }
                    temp.append(OPTIONAL_WHITE_SPACE).append("\\)");
                    valueListInRightOrder.add(temp.toString());
                }

                buffer.append(OrderRotation.useOrDefault(this.insertIntoValuesOrder, valueListInRightOrder));
                buffer.append(columnsOrderOptionsIterator.hasNext() ? "|" : "");
            }
            buffer.append(")");
        } else if (insert.getColumns() == null && insert.getItemsList(ExpressionList.class) != null) {
            insert.getItemsList(ExpressionList.class).accept(this);
        } else if (insert.getColumns() != null && (insert.getItemsList(MultiExpressionList.class) == null
                && insert.getItemsList(ExpressionList.class) == null)) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
            List<String> columnsAsStringList = new ArrayList<>();
            for (Column column : insert.getColumns()) {
                columnsAsStringList.add(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, column.toString()));
            }
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, columnsAsStringList));
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
        }

        if (insert.getOutputClause() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "OUTPUT"));
            buffer.append(REQUIRED_WHITE_SPACE);
            List<String> outputClauses = new ArrayList<>();
            for(SelectItem selectItem : insert.getOutputClause().getSelectItemList()){
                outputClauses.add(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, selectItem.toString()));
            }
            buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, outputClauses));
        }

        if (insert.getSelect() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            if (insert.getSelect().getWithItemsList() != null) {
                buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "WITH"));
                buffer.append(REQUIRED_WHITE_SPACE);
                for (WithItem with : insert.getSelect().getWithItemsList()) {
                    with.accept(this.selectDeParserForRegEx);
                }
                buffer.append(REQUIRED_WHITE_SPACE);
            }
        }

        if (insert.isUseSet()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "SET"));
            buffer.append(REQUIRED_WHITE_SPACE);
            List<String> useSets = new LinkedList<>();
            StringBuilder tempUseSet = new StringBuilder();
            ExpressionDeParserForRegEx tmpExpressionDeParserForRegEx = new ExpressionDeParserForRegEx(this.selectDeParserForRegEx, tempUseSet, this.settings);
            for (int i = 0; i < insert.getSetColumns().size(); i++) {
                Column column = insert.getSetColumns().get(i);
                column.accept(tmpExpressionDeParserForRegEx);
                tempUseSet.append(OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);
                Expression expression = insert.getSetExpressionList().get(i);
                expression.accept(tmpExpressionDeParserForRegEx);
                useSets.add(tempUseSet.toString());
                tempUseSet.replace(0, tempUseSet.length(),"");
            }
            buffer.append(OrderRotation.useOrDefault(this.columnNameOrder, useSets));
        }

        if (insert.isUseDuplicate()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ON"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "DUPLICATE"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "KEY"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "UPDATE"));
            buffer.append(REQUIRED_WHITE_SPACE);

            for (int i = 0; i < insert.getDuplicateUpdateColumns().size(); i++) {
                Column column = insert.getDuplicateUpdateColumns().get(i);
                buffer.append(column.getFullyQualifiedName()).append(OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);

                Expression expression = insert.getDuplicateUpdateExpressionList().get(i);
                expression.accept(this.expressionDeParserForRegEx);
                if (i < insert.getDuplicateUpdateColumns().size() - 1) {
                    buffer.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
                }
            }
        }

        if (insert.getReturningExpressionList() != null) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "RETURNING"));
            buffer.append(REQUIRED_WHITE_SPACE);
            List<String> returningExpressionsAsStringList = new ArrayList<>();
            for (SelectItem selectItem : insert.getReturningExpressionList()) {
                returningExpressionsAsStringList.add(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, selectItem.toString()));
            }
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, returningExpressionsAsStringList));
        }
    }

    /**
     *Performs  {@link NamedExpressionList} deparsing.
     * @param namedExpressionList {@link NamedExpressionList}
     * @throws UnsupportedOperationException forbidden in this implementation
     */
    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        throw new UnsupportedOperationException();
    }

    /**
     * Performs {@link ExpressionList} deparsing.
     * @param expressionList {@link ExpressionList}
     */
    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(
                SpellingMistake.useOrDefault(
                        this.keywordSpellingMistake,
                        "VALUE")
        ).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> expressionListAsString = new ArrayList<>();
        prepareExpressionListForOrderRotation(expressionList, expressionListAsString);
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, expressionListAsString));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    /**
     * Performs {@link MultiExpressionList} deparsing.
     * @param multiExprList {@link MultiExpressionList}
     */
    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(
                SpellingMistake.useOrDefault(
                        this.keywordSpellingMistake,
                        "VALUE")
        ).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> multiExpressionListAsString = new ArrayList<>();
        for (ExpressionList expressionList : multiExprList.getExpressionLists()) {
            List<String> expressionListAsString = new ArrayList<>();
            prepareExpressionListForOrderRotation(expressionList, expressionListAsString);
            String singleValueListLine = OPTIONAL_WHITE_SPACE
                    + "\\("
                    + OrderRotation.useOrDefault(this.tableNameOrder, expressionListAsString)
                    + "\\)"
                    + OPTIONAL_WHITE_SPACE;
            multiExpressionListAsString.add(singleValueListLine);
        }
        buffer.append(OrderRotation.useOrDefault(this.tableNameOrder, multiExpressionListAsString));
    }

    /**
     * Performs {@link SubSelect} deparsing.
     * @param subSelect {@link SubSelect}
     */
    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this.selectDeParserForRegEx);
    }

    /**
     * It maps columns and related values. Writes in a map, instantiated in {@link InsertDeParserForRegEx#deParse(Insert)}.
     * Returns a string list with all columns.
     * @param mappedColumnsAndRelatedValues map with string (columns) as keys and list of string as values for the mapped values
     * @param insert {@link Insert}
     * @return column list
     */
    private String[] generateListOfColumnsOrderOptionForMultipleValues(Map<String, List<String>> mappedColumnsAndRelatedValues, Insert insert) {
        for (int i = 0; i < insert.getColumns().size(); i++) {
            List<Expression> expression = insert.getItemsList(ExpressionList.class).getExpressions();
            List<String> valueList = new ArrayList<>();
            for (Expression exp : expression) {
                valueList.add(exp.toString().replace("(", "").replace(")", "").replace(" ", "").split(",")[i]);
            }
            mappedColumnsAndRelatedValues.put(
                    insert.getColumns().get(i).toString(),
                    valueList
            );
        }
        return this.generateColumnsRotatedArray(mappedColumnsAndRelatedValues.keySet());
    }

    /**
     * It maps columns and related values. Writes in a map, instantiated in {@link InsertDeParserForRegEx#deParse(Insert)}.
     * Returns a string list with all columns.
     * @param mappedColumnsAndRelatedValues map with string (columns) as keys and list of string as values for the mapped values
     * @param insert {@link Insert}
     * @return column list
     */
    private String[] generateListOfColumnsOrderOption(Map<String, String> mappedColumnsAndRelatedValues, Insert insert) {
        for (int i = 0; i < insert.getColumns().size(); i++) {
            mappedColumnsAndRelatedValues.put(
                    insert.getColumns().get(i).toString(),
                    insert.getItemsList(ExpressionList.class).getExpressions().get(i).toString()
            );
        }
        return this.generateColumnsRotatedArray(mappedColumnsAndRelatedValues.keySet());
    }

    /**
     * Rotates column name orders.
     * @param keySet set of string
     * @return list of strings with all possible orders
     */
    private String[] generateColumnsRotatedArray(Set<String> keySet) {
        List<String> mappedColumnsAndRelatedValuesKeySet = new ArrayList<>(keySet);
        String columnsRotated = OrderRotation.useOrDefault(this.columnNameOrder, mappedColumnsAndRelatedValuesKeySet);
        columnsRotated = columnsRotated.replace(OPTIONAL_WHITE_SPACE, "").replace("(?:", "").replace(")", "");
        return columnsRotated.split("\\|");
    }

    /**
     * Rotates column name orders.
     * @param columnsOrderOptionsIterator string {@link Iterator} for column name orders
     * @return list of strings with all possible orders with finalized regex and appended value keyword
     */
    private String[] generateColumnArray(Iterator<String> columnsOrderOptionsIterator) {
        String singleColumnOrderOption = columnsOrderOptionsIterator.next();

        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        String[] splittedSingleColumnOrderOption = singleColumnOrderOption.replace(" ", "").split(",");
        Iterator<String> stringIterator = Arrays.stream(splittedSingleColumnOrderOption).iterator();
        while (stringIterator.hasNext()) {
            buffer.append(SpellingMistake.useOrDefault(this.columnNameSpellingMistake, stringIterator.next()));
            if (stringIterator.hasNext()) buffer.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "VALUE")).append("S?").append(OPTIONAL_WHITE_SPACE);
        return singleColumnOrderOption.replace(" ", "").split(",");
    }

    /**
     * Prepares set-expressions for deparsing.
     * @param expressionList {@link ExpressionList}
     * @param expressionListAsString list of strings
     */
    private void prepareExpressionListForOrderRotation(ExpressionList expressionList, List<String> expressionListAsString) {
        for (Expression expression : expressionList.getExpressions()) {
            String expressionFixed = expression.toString();
            boolean hasQuotationMarks = false;
            for (String str : this.selectDeParserForRegEx.getQuotationMarkList()) {
                if (expressionFixed.contains(str)) {
                    hasQuotationMarks = true;
                    break;
                }
            }
            if (hasQuotationMarks) {
                expressionFixed = this.selectDeParserForRegEx.generateRegExForQuotationMarks() + "?"
                        + SpellingMistake.useOrDefault(this.columnNameSpellingMistake, expressionFixed.replaceAll(this.selectDeParserForRegEx.generateRegExForQuotationMarks(), ""))
                        + this.selectDeParserForRegEx.generateRegExForQuotationMarks() + "?";
            } else {
                expressionFixed = this.selectDeParserForRegEx.generateRegExForQuotationMarks() + "?"
                        + SpellingMistake.useOrDefault(this.columnNameSpellingMistake, expressionFixed)
                        + this.selectDeParserForRegEx.generateRegExForQuotationMarks() + "?";
            }
            expressionListAsString.add(expressionFixed);
        }
    }


}