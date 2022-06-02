package sqltoregex.deparser;

import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class StatementDeParserForRegEx extends StatementDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private final SpellingMistake keywordSpellingMistake;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    SettingsContainer settings;

    public StatementDeParserForRegEx(StringBuilder buffer, SettingsContainer settings) {
        this(new ExpressionDeParserForRegEx(settings), buffer, settings);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                     SettingsContainer settings) {
        this(expressionDeParser, new SelectDeParserForRegEx(settings), buffer, settings);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser,
                                     SelectDeParserForRegEx selectDeParser, StringBuilder buffer,
                                     SettingsContainer settings) {
        super(expressionDeParser, selectDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = selectDeParser;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.settings = settings;
    }

    @Override
    public void visit(Select select) {
        this.selectDeParserForRegEx.setBuffer(this.buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(this.buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegEx);
        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
            this.buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "WITH"));
            this.buffer.append(REQUIRED_WHITE_SPACE);
            this.buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(select));
        }
        select.getSelectBody().accept(selectDeParserForRegEx);
    }

    @Override
    public void visit(Insert insert) {
        this.selectDeParserForRegEx.setBuffer(this.buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(this.buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(
                this.expressionDeParserForRegEx,
                this.selectDeParserForRegEx,
                this.buffer,
                this.settings);
        insertDeParserForRegEx.deParse(insert);
    }

    @Override
    public void visit(CreateTable createTable) {
        CreateTableDeParserForRegEx createTableDeParserForRegEx = new CreateTableDeParserForRegEx(this, buffer, settings);
        createTableDeParserForRegEx.deParse(createTable);
    }

    @Override
    public void visit(Update update) {
        this.selectDeParserForRegEx.setBuffer(this.buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(this.buffer);
        UpdateDeParserForRegEx updateDeParser = new UpdateDeParserForRegEx(
                this.expressionDeParserForRegEx,
                this.selectDeParserForRegEx,
                this.buffer,
                this.settings);
        this.selectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
        updateDeParser.deParse(update);
    }

    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }
}
