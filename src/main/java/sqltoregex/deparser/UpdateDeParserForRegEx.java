package sqltoregex.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdateDeParserForRegEx extends UpdateDeParser {
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private final SpellingMistake keywordSpellingMistake;
    private final OrderRotation columnNameOrderRotation;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    SettingsManager settingsManager;

    public UpdateDeParserForRegEx(SettingsManager settingsManager, StringBuilder buffer) {
        this(new ExpressionDeParserForRegEx(settingsManager), new SelectDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public UpdateDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParserForRegEx, SelectDeParserForRegEx selectDeParserForRegEx, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParserForRegEx, buffer);
        this.settingsManager = settingsManager;
        this.expressionDeParserForRegEx = expressionDeParserForRegEx;
        this.selectDeParserForRegEx = selectDeParserForRegEx;
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                SpellingMistake.class).orElse(null);
        this.columnNameOrderRotation = settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER,
                OrderRotation.class).orElse(null);
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
            buffer.append(update.getModifierPriority()).append(" ");
        }
        if (update.isModifierIgnore()) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(false, "IGNORE", true);
        }
        buffer.append(update.getTable());
        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
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
                singleSet.append(columnIterator.next().toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
                if(columnIterator.hasNext()) singleSet.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            singleSet.append(OPTIONAL_WHITE_SPACE + "\\)?" + OPTIONAL_WHITE_SPACE);

            singleSet.append(OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);

            singleSet.append(OPTIONAL_WHITE_SPACE + "\\(?" + OPTIONAL_WHITE_SPACE);
            Iterator<Expression> expressionIterator = updateSet.getExpressions().iterator();
            while(expressionIterator.hasNext()){
                singleSet.append(expressionIterator.next().toString().concat(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE));
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
            buffer.append(update.getFromItem());
            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    if (join.isSimple()) {
                        buffer.append(", ").append(join);
                    } else {
                        buffer.append(" ").append(join);
                    }
                }
            }
        }

        if (update.getWhere() != null) {
            this.setKeywordSpellingMistakeWithRequiredWhitespaces(true, "WHERE", true);
            update.getWhere().accept(this.getExpressionDeParserForRegEx());
        }
        if (update.getOrderByElements() != null) {
            new OrderByDeParserForRegEx(this.getExpressionDeParserForRegEx(), buffer, this.settingsManager).deParse(update.getOrderByElements(), update.getFromItem());
        }
        if (update.getLimit() != null) {
            new LimitDeParserForRegEx(buffer, this.settingsManager).deParse(update.getLimit());
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
}
