package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
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
    public static final String INSERT = "INSERT";
    public static final String IGNORE = "IGNORE";
    public static final String INTO = "INTO";
    public static final String WITH = "WITH";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    public static final String VALUES = "VALUES";
    public static final String VALUE = "VALUE";
    List<String> quotationMarkList = Arrays.asList("'", "`", "\"");
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    RegExGenerator<String> keywordSpellingMistake;
    RegExGenerator<String> tableNameSpellingMistake;
    RegExGenerator<List<String>> tableNameOrder;

    public InsertDeParserForRegEx(SettingsManager settingsManager) {
        this(new ExpressionDeParserForRegEx(settingsManager), new SelectDeParserForRegEx(settingsManager), new StringBuilder(), settingsManager);
    }

    public InsertDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, selectDeParserForRegEx, buffer);
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
        this.setKeywordSpellingMistake(settingsManager);
        this.setTableNameSpellingMistake(settingsManager);
        this.setTableNameOrder(settingsManager);
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

    private void setKeywordSpellingMistake(SettingsManager settingsManager) {
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class).orElse(null);
    }

    private String useKeywordSpellingMistake(String str) {
        if (null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setTableNameSpellingMistake(SettingsManager settingsManager) {
        this.tableNameSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING, SpellingMistake.class).orElse(null);
    }

    private String useTableNameSpellingMistake(String str) {
        if (null != this.tableNameOrder) return this.tableNameSpellingMistake.generateRegExFor(str);
        else return str;
    }

    private void setTableNameOrder(SettingsManager settingsManager) {
        this.tableNameOrder = settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER, OrderRotation.class).orElse(null);
    }

    private String useTableNameOrder(List<String> strlist) {
        if (null != this.tableNameOrder) return this.tableNameOrder.generateRegExFor(strlist);
        return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, strlist);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void deParse(Insert insert) {
        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
            buffer.append(useKeywordSpellingMistake(WITH));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(insert));
        }

        buffer.append(useKeywordSpellingMistake(INSERT)).append(REQUIRED_WHITE_SPACE);
        if (insert.getModifierPriority() != null) {
            buffer.append(insert.getModifierPriority()).append(REQUIRED_WHITE_SPACE);
        }
        if (insert.isModifierIgnore()) {
            buffer.append(useKeywordSpellingMistake(IGNORE)).append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(useKeywordSpellingMistake(INTO)).append(REQUIRED_WHITE_SPACE);
        buffer.append(useTableNameSpellingMistake(insert.getTable().toString())).append(REQUIRED_WHITE_SPACE);

        if (insert.getColumns() != null && insert.getItemsList() != null && insert.getItemsList() instanceof ExpressionList) {
            Map<String, String> mappedColumnsAndRelatedValues = new HashMap<>();
            for (int i = 0; i < insert.getColumns().size(); i++) {
                mappedColumnsAndRelatedValues.put(
                        insert.getColumns().get(i).toString(),
                        ((ExpressionList) insert.getItemsList()).getExpressions().get(i).toString()
                );
            }
            List<String> mappedColumnsAndRelatedValuesKeySet = new ArrayList<>();
            for(String string : mappedColumnsAndRelatedValues.keySet()){
                mappedColumnsAndRelatedValuesKeySet.add(string.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            String columnsRotated = useTableNameOrder(mappedColumnsAndRelatedValuesKeySet);
            columnsRotated = columnsRotated.replace(OPTIONAL_WHITE_SPACE, "").replace("(?:", "").replace(")", "");
            String[] columnsOrderOptionsAsStringSet = columnsRotated.split("\\|");

            buffer.append("(?:");
            Iterator<String> columnsOrderOptionsIterator = Arrays.stream(columnsOrderOptionsAsStringSet).iterator();
            while (columnsOrderOptionsIterator.hasNext()) {
                String singleColumnOrderOption = columnsOrderOptionsIterator.next();
                buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
                buffer.append(singleColumnOrderOption.replace(",", OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE));
                buffer.append(OPTIONAL_WHITE_SPACE).append("\\)").append(OPTIONAL_WHITE_SPACE);
                buffer.append(useKeywordSpellingMistake(VALUE)).append("S?").append(OPTIONAL_WHITE_SPACE).append("\\(");
                String[] extractedColumnsInOrderOption = singleColumnOrderOption.replace(" ", "").split(",");

                Iterator<String> extractedColumnsIterator = Arrays.stream(extractedColumnsInOrderOption).iterator();

                buffer.append(OPTIONAL_WHITE_SPACE);
                while (extractedColumnsIterator.hasNext()) {
                    buffer.append(mappedColumnsAndRelatedValues.get(extractedColumnsIterator.next()));
                    if (extractedColumnsIterator.hasNext()) {
                        buffer.append(OPTIONAL_WHITE_SPACE);
                        buffer.append(",");
                        buffer.append(OPTIONAL_WHITE_SPACE);
                    }
                }
                buffer.append(OPTIONAL_WHITE_SPACE);
                buffer.append("\\)");
                buffer.append("|");
            }

            buffer.append(")");

        } else if (insert.getColumns() == null && insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        } else if (insert.getColumns() != null && insert.getItemsList() == null){
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
            List<String> columnsAsStringList = new ArrayList<>();
            for (Column column : insert.getColumns()) {
                columnsAsStringList.add(column.toString());
            }
            buffer.append(useTableNameOrder(columnsAsStringList));
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
        }

//        if (insert.getColumns() != null) {
//             buffer.append(REQUIRED_WHITE_SPACE);
//             buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
//             List<String> columnsAsStringList = new ArrayList<>();
//             for (Column column : insert.getColumns()) {
//                columnsAsStringList.add(column.toString());
//             }
//             buffer.append(useTableNameOrder(columnsAsStringList));
//             buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
//        }
//
//        if (insert.getItemsList() != null) {
//            insert.getItemsList().accept(this);
//        }

        if (insert.getSelect() != null) {
            buffer.append(" ");
            if (insert.getSelect().getWithItemsList() != null) {
                buffer.append("WITH ");
                for (WithItem with : insert.getSelect().getWithItemsList()) {
                    with.accept(this.selectDeParserForRegEx);
                }
                buffer.append(" ");
            }
            insert.getSelect().getSelectBody().accept(this.selectDeParserForRegEx);
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
            for (Iterator<SelectExpressionItem> iter = insert.getReturningExpressionList().iterator(); iter
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
        buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake(VALUE)).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> expressionListAsString = new ArrayList<>();
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
                        + useTableNameSpellingMistake(expressionFixed.replaceAll(this.generateRegExForQuotationMarks(), ""))
                        + this.generateRegExForQuotationMarks();
            } else {
                expressionFixed = this.generateRegExForQuotationMarks()
                        + useTableNameSpellingMistake(expressionFixed)
                        + this.generateRegExForQuotationMarks();
            }
            expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(useTableNameOrder(expressionListAsString));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake(VALUE)).append("S?").append(REQUIRED_WHITE_SPACE);
        List<String> multiExpressionListAsString  = new ArrayList<>();
        for (ExpressionList expressionList : multiExprList.getExpressionLists()) {
            List<String> expressionListAsString = new ArrayList<>();
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
                            + useTableNameSpellingMistake(expressionFixed.replaceAll(this.generateRegExForQuotationMarks(), ""))
                            + this.generateRegExForQuotationMarks();
                } else {
                    expressionFixed = this.generateRegExForQuotationMarks()
                            + useTableNameSpellingMistake(expressionFixed)
                            + this.generateRegExForQuotationMarks();
                }
                expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            String singleValueListLine = OPTIONAL_WHITE_SPACE + "\\(" + useTableNameOrder(expressionListAsString) + "\\)" + OPTIONAL_WHITE_SPACE;
            multiExpressionListAsString.add(singleValueListLine.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        buffer.append(useTableNameOrder(multiExpressionListAsString));
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this.selectDeParserForRegEx);
    }
}