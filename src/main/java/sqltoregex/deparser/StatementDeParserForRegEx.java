package sqltoregex.deparser;

import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class StatementDeParserForRegEx extends StatementDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private final SettingsManager settingsManager;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    IRegExGenerator<String> keywordSpellingMistake;


    public StatementDeParserForRegEx(StringBuilder buffer, SettingsManager settingsManager) {
        this(new ExpressionDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                     SettingsManager settingsManager) {
        this(expressionDeParser, new SelectDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser,
                                     SelectDeParserForRegEx selectDeParser, StringBuilder buffer,
                                     SettingsManager settingsManager) {
        super(expressionDeParser, selectDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = selectDeParser;
        this.settingsManager = settingsManager;
        this.setKeywordSpellingMistake(settingsManager);
    }

    private void setKeywordSpellingMistake(SettingsManager settingsManager) {
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING,
                                                                                 SpellingMistake.class).orElse(null);
    }

    private String useKeywordSpellingMistake(String str) {
        if (null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    @Override
    public void visit(Select select) {
        this.selectDeParserForRegEx.setBuffer(buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegEx);
        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
            buffer.append(useKeywordSpellingMistake("WITH"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(select));
        }
        select.getSelectBody().accept(selectDeParserForRegEx);
    }

    @Override
    public void visit(Insert insert) {
        this.selectDeParserForRegEx.setBuffer(buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(this.expressionDeParserForRegEx, this.selectDeParserForRegEx, buffer, this.settingsManager);
        insertDeParserForRegEx.deParse(insert);
    }

    @Override
    public void visit(Update update) {
        this.selectDeParserForRegEx.setBuffer(buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(buffer);
        UpdateDeParserForRegEx updateDeParser = new UpdateDeParserForRegEx(this.expressionDeParserForRegEx, buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
        updateDeParser.deParse(update);
    }

    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }
}
