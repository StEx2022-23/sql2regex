package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
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
    public void visit(ExpressionList expressionList) {
        buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake(VALUES)).append(REQUIRED_WHITE_SPACE);
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
                expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            } else {
                expressionFixed = this.generateRegExForQuotationMarks()
                        + useTableNameSpellingMistake(expressionFixed)
                        + this.generateRegExForQuotationMarks();
                expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
            }
            expressionListAsString.add(expressionFixed.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        buffer.append(useTableNameOrder(expressionListAsString));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(REQUIRED_WHITE_SPACE).append(useKeywordSpellingMistake(VALUES)).append(REQUIRED_WHITE_SPACE);
        buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
        List<String> multiExpressionListAsString  = new ArrayList<>();
        for (ExpressionList expressionList : multiExprList.getExpressionLists()) {
            List<String> expressionListAsString = new ArrayList<>();
            for (Expression expression : expressionList.getExpressions()) {
                expressionListAsString.add(expression.toString());
            }
            String singleValueListLine = OPTIONAL_WHITE_SPACE
                    + "\\("
                    + OPTIONAL_WHITE_SPACE
                    + useTableNameOrder(expressionListAsString)
                    + OPTIONAL_WHITE_SPACE
                    + "\\)"
                    + OPTIONAL_WHITE_SPACE;
            multiExpressionListAsString.add(singleValueListLine.concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
        }
        buffer.append(useTableNameOrder(multiExpressionListAsString));
        buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this.selectDeParserForRegEx);
    }
}


//    @Override
//    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
//    public void deParse(Insert insert) {
//        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
//            buffer.append(useKeywordSpellingMistake(WITH));
//            buffer.append(REQUIRED_WHITE_SPACE);
//            buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(insert));
//        }
//
//        buffer.append(useKeywordSpellingMistake(INSERT)).append(REQUIRED_WHITE_SPACE);
//        if (insert.getModifierPriority() != null) {
//            buffer.append(insert.getModifierPriority()).append(REQUIRED_WHITE_SPACE);
//        }
//        if (insert.isModifierIgnore()) {
//            buffer.append(useKeywordSpellingMistake(IGNORE)).append(REQUIRED_WHITE_SPACE);
//        }
//        buffer.append(useKeywordSpellingMistake(INTO)).append(REQUIRED_WHITE_SPACE);
//        buffer.append(useTableNameSpellingMistake(insert.getTable().toString()));
//
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
//
////            if (insert.getOutputClause() != null) {
////                buffer.append(insert.getOutputClause().toString());
////            }
//
//            if (insert.getSelect() != null) {
//                buffer.append(" ");
////                if (insert.getSelect().isUsingWithBrackets()) {
////                    buffer.append("(");
////                }
//                if (insert.getSelect().getWithItemsList() != null) {
//                    buffer.append("WITH ");
//                    for (WithItem with : insert.getSelect().getWithItemsList()) {
//                        with.accept(selectDeParserForRegEx);
//                    }
//                    buffer.append(" ");
//                }
//                insert.getSelect().getSelectBody().accept(selectDeParserForRegEx);
////                if (insert.getSelect().isUsingWithBrackets()) {
////                    buffer.append(")");
////                }
//            }
//
//            if (insert.isUseSet()) {
//                buffer.append(" SET ");
//                for (int i = 0; i < insert.getSetColumns().size(); i++) {
//                    Column column = insert.getSetColumns().get(i);
//                    column.accept(expressionDeParserForRegEx);
//
//                    buffer.append(" = ");
//
//                    Expression expression = insert.getSetExpressionList().get(i);
//                    expression.accept(expressionDeParserForRegEx);
//                    if (i < insert.getSetColumns().size() - 1) {
//                        buffer.append(", ");
//                    }
//                }
//            }
//
//            if (insert.isUseDuplicate()) {
//                buffer.append(" ON DUPLICATE KEY UPDATE ");
//                for (int i = 0; i < insert.getDuplicateUpdateColumns().size(); i++) {
//                    Column column = insert.getDuplicateUpdateColumns().get(i);
//                    buffer.append(column.getFullyQualifiedName()).append(" = ");
//
//                    Expression expression = insert.getDuplicateUpdateExpressionList().get(i);
//                    expression.accept(expressionDeParserForRegEx);
//                    if (i < insert.getDuplicateUpdateColumns().size() - 1) {
//                        buffer.append(", ");
//                    }
//                }
//            }
//
//            if (insert.getReturningExpressionList() != null) {
//                buffer.append(" RETURNING ").append(PlainSelect.
//                        getStringList(insert.getReturningExpressionList(), true, false));
//            }
//        }
//

//
//    @Override
//    public void visit(NamedExpressionList namedExpressionList) {
//        throw new UnsupportedOperationException();
//    }
//

//

//}
