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
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.*;

public class InsertDeParserForRegEx extends InsertDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    List<String> quotationMarkList = Arrays.asList("'", "`", "\"");
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final OrderRotation tableNameOrder;

    public InsertDeParserForRegEx(SettingsManager settingsManager) {
        this(new ExpressionDeParserForRegEx(settingsManager), new SelectDeParserForRegEx(settingsManager), new StringBuilder(), settingsManager);
    }

    public InsertDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, selectDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                SpellingMistake.class).orElse(null);
        this.tableNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER,
                OrderRotation.class).orElse(null);
        this.tableNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING,
                SpellingMistake.class).orElse(null);
    }

    private String generateRegExForQuotationMarks(){
        StringBuilder str = new StringBuilder();
        str.append("[");
        for(String quotationMark : this.quotationMarkList){
            str.append(quotationMark);
        }
        str.append("]");
        return str.toString();
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void deParse(Insert insert) {
        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "WITH"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(insert));
        }

        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"INSERT")).append(REQUIRED_WHITE_SPACE);
        if (insert.getModifierPriority() != null) {
            buffer.append(insert.getModifierPriority()).append(REQUIRED_WHITE_SPACE);
        }
        if (insert.isModifierIgnore()) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"IGNORE")).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"INTO")).append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake,insert.getTable().toString())).append(REQUIRED_WHITE_SPACE);

        if (insert.getColumns() != null && !(insert.getItemsList(ExpressionList.class).getExpressions().get(0) instanceof RowConstructor)) {
            Map<String, String> mappedColumnsAndRelatedValues = new HashMap<>();
            buffer.append("(?:");
            Iterator<String> columnsOrderOptionsIterator = Arrays.stream(this.generateListOfColumnsOrderOption(mappedColumnsAndRelatedValues, insert)).iterator();
            while (columnsOrderOptionsIterator.hasNext()) {
                Iterator<String> extractedColumnsIterator = Arrays.stream(this.generateColumnArray(columnsOrderOptionsIterator)).iterator();
                buffer.append("\\(");
                buffer.append(OPTIONAL_WHITE_SPACE);
                while (extractedColumnsIterator.hasNext()) {
                    buffer.append(this.generateRegExForQuotationMarks());
                    buffer.append(mappedColumnsAndRelatedValues.get(extractedColumnsIterator.next()).replaceAll(this.generateRegExForQuotationMarks(), ""));
                    buffer.append(this.generateRegExForQuotationMarks());
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
            Map<String, List<String>> mappedColumnsAndRelatedValues = new HashMap<>();
            buffer.append("(?:");
            Iterator<String> columnsOrderOptionsIterator = Arrays.stream(this.generateListOfColumnsOrderOptionForMultipleValues(mappedColumnsAndRelatedValues, insert)).iterator();
            while (columnsOrderOptionsIterator.hasNext()) {
                Iterator<String> extractedColumnsIterator = Arrays.stream(this.generateColumnArray(columnsOrderOptionsIterator)).iterator();
                List<String> tempValuesRelatedToActualCol = new ArrayList<>();
                int valueDepth = 0;
                while(extractedColumnsIterator.hasNext()){
                    String tempCol = extractedColumnsIterator.next();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(tempCol).append(",");
                    for(String str : mappedColumnsAndRelatedValues.get(tempCol)){
                        stringBuilder.append(str).append(",");
                    }
                    stringBuilder.replace(stringBuilder.toString().length() - 1, stringBuilder.toString().length(), "");
                    tempValuesRelatedToActualCol.add(stringBuilder.toString());
                    valueDepth = mappedColumnsAndRelatedValues.get(tempCol).size();
                }

                List<String> valueListInRightOrder = new ArrayList<>();
                for(int i = 0; i < valueDepth; i++){
                    StringBuilder temp = new StringBuilder();
                    temp.append("\\(").append(OPTIONAL_WHITE_SPACE);
                    Iterator<String> stringIterator = tempValuesRelatedToActualCol.iterator();
                    while (stringIterator.hasNext()){
                        temp.append(this.generateRegExForQuotationMarks());
                        temp.append(stringIterator.next().split(",")[i + 1].replaceAll(this.generateRegExForQuotationMarks(), ""));
                        temp.append(this.generateRegExForQuotationMarks());
                        if(stringIterator.hasNext()) temp.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
                    }
                    temp.append(OPTIONAL_WHITE_SPACE).append("\\)");
                    valueListInRightOrder.add(temp.toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
                }

                buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder, valueListInRightOrder));
                buffer.append(columnsOrderOptionsIterator.hasNext() ? "|" : "");
            }
            buffer.append(")");
        } else if (insert.getColumns() == null && (insert.getItemsList(MultiExpressionList.class) != null && insert.getItemsList(ExpressionList.class) != null)) {
            insert.getItemsList(MultiExpressionList.class).accept(this);
            insert.getItemsList(ExpressionList.class).accept(this);
        } else if (insert.getColumns() != null && (insert.getItemsList(MultiExpressionList.class) == null && insert.getItemsList(ExpressionList.class) == null)){
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
            List<String> columnsAsStringList = new ArrayList<>();
            for (Column column : insert.getColumns()) {
                columnsAsStringList.add(column.toString());
            }
            buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder,columnsAsStringList));
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
        }

        if (insert.getSelect() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            if (insert.getSelect().getWithItemsList() != null) {
                buffer.append("WITH ");
                for (WithItem with : insert.getSelect().getWithItemsList()) {
                    with.accept(this.selectDeParserForRegEx);
                }
                buffer.append(" ");
            }
        }

        if (insert.isUseSet()) {
            buffer.append(" SET ");
            for (int i = 0; i < insert.getSetColumns().size(); i++) {
                Column column = insert.getSetColumns().get(i);
                column.accept(this.expressionDeParserForRegEx);

                buffer.append(" = ");

                Expression expression = insert.getSetExpressionList().get(i);
                expression.accept(this.expressionDeParserForRegEx);
                if (i < insert.getSetColumns().size() - 1) {
                    buffer.append(", ");
                }
            }
        }

        if (insert.isUseDuplicate()) {
            buffer.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < insert.getDuplicateUpdateColumns().size(); i++) {
                Column column = insert.getDuplicateUpdateColumns().get(i);
                buffer.append(column.getFullyQualifiedName()).append(" = ");

                Expression expression = insert.getDuplicateUpdateExpressionList().get(i);
                expression.accept(this.expressionDeParserForRegEx);
                if (i < insert.getDuplicateUpdateColumns().size() - 1) {
                    buffer.append(", ");
                }
            }
        }

        if (insert.getReturningExpressionList() != null) {
            buffer.append(" RETURNING ");
            for (Iterator<SelectItem> iter = insert.getReturningExpressionList().iterator(); iter
                    .hasNext();) {
                buffer.append(iter.next().toString());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(REQUIRED_WHITE_SPACE).append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"VALUE")).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> expressionListAsString = new ArrayList<>();
        prepareExpressionListForOrderRotation(expressionList, expressionListAsString);
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder,expressionListAsString));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(REQUIRED_WHITE_SPACE).append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"VALUE")).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> multiExpressionListAsString  = new ArrayList<>();
        for (ExpressionList expressionList : multiExprList.getExpressionLists()) {
            List<String> expressionListAsString = new ArrayList<>();
            prepareExpressionListForOrderRotation(expressionList, expressionListAsString);
            String singleValueListLine = OPTIONAL_WHITE_SPACE + "\\(" + RegExGenerator.useOrderRotation(this.tableNameOrder,expressionListAsString) + "\\)" + OPTIONAL_WHITE_SPACE;
            multiExpressionListAsString.add(singleValueListLine.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        buffer.append(RegExGenerator.useOrderRotation(this.tableNameOrder, multiExpressionListAsString));
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this.selectDeParserForRegEx);
    }

    private String[] generateListOfColumnsOrderOptionForMultipleValues(Map<String, List<String>> mappedColumnsAndRelatedValues, Insert insert){
        for (int i = 0; i < insert.getColumns().size(); i++) {
            List<Expression> expression = insert.getItemsList(ExpressionList.class).getExpressions();
            List<String> valueList = new ArrayList<>();
            for(Expression exp : expression) {
                valueList.add(exp.toString().replace("(", "").replace(")", "").replace(" ", "").split(",")[i]);
            }
            mappedColumnsAndRelatedValues.put(
                    insert.getColumns().get(i).toString(),
                    valueList
            );
        }
        return this.generateColumnsRotatedArray(mappedColumnsAndRelatedValues.keySet());
    }

    private String[] generateListOfColumnsOrderOption(Map<String, String> mappedColumnsAndRelatedValues, Insert insert){
        for (int i = 0; i < insert.getColumns().size(); i++) {
            mappedColumnsAndRelatedValues.put(
                    insert.getColumns().get(i).toString(),
                    insert.getItemsList(ExpressionList.class).getExpressions().get(i).toString()
            );
        }
        return this.generateColumnsRotatedArray(mappedColumnsAndRelatedValues.keySet());
    }

    private String[] generateColumnsRotatedArray(Set<String> keySet){
        List<String> mappedColumnsAndRelatedValuesKeySet = new ArrayList<>();
        for (String string : keySet) {
            mappedColumnsAndRelatedValuesKeySet.add(string.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        String columnsRotated = RegExGenerator.useOrderRotation(this.tableNameOrder, mappedColumnsAndRelatedValuesKeySet);
        columnsRotated = columnsRotated.replace(OPTIONAL_WHITE_SPACE, "").replace("(?:", "").replace(")", "");
        return columnsRotated.split("\\|");
    }

    private String[] generateColumnArray(Iterator<String> columnsOrderOptionsIterator){
        String singleColumnOrderOption = columnsOrderOptionsIterator.next();
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(singleColumnOrderOption.replace(",", OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake,"VALUE")).append("S?").append(OPTIONAL_WHITE_SPACE);
        return singleColumnOrderOption.replace(" ", "").split(",");
    }

    private void prepareExpressionListForOrderRotation(ExpressionList expressionList, List<String> expressionListAsString){
        for (Expression expression : expressionList.getExpressions()) {
            String expressionFixed = expression.toString();
            boolean hasQuotationMarks = false;
            for(String str : this.quotationMarkList){
                if (expressionFixed.contains(str)) {
                    hasQuotationMarks = true;
                    break;
                }
            }
            if(hasQuotationMarks){
                expressionFixed = this.generateRegExForQuotationMarks()
                        + RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake, expressionFixed.replaceAll(this.generateRegExForQuotationMarks(), ""))
                        + this.generateRegExForQuotationMarks();
            } else {
                expressionFixed = this.generateRegExForQuotationMarks()
                        + RegExGenerator.useSpellingMistake(this.tableNameSpellingMistake,expressionFixed)
                        + this.generateRegExForQuotationMarks();
            }
            expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
    }
}