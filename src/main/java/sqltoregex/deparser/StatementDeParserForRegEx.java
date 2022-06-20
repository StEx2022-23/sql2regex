package sqltoregex.deparser;

import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

/**
 * Implements an own statement deparser to generate regular expressions.
 */
public class StatementDeParserForRegEx extends StatementDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    public static final String QUOTATION_MARK_REGEX = "[`´'\"]";
    private final SpellingMistake keywordSpellingMistake;
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    SettingsContainer settings;

    /**
     * Short constructor for StatementDeParserForRegEx. Inits the expanded constructor.
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public StatementDeParserForRegEx(StringBuilder buffer, SettingsContainer settings) {
        this(new ExpressionDeParserForRegEx(settings), buffer, settings);
    }

    /**
     * Shorter constructor for StatementDeParserForRegEx. Inits the expanded constructor.
     * @param expressionDeParser {@link ExpressionDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer,
                                     SettingsContainer settings) {
        this(expressionDeParser, new SelectDeParserForRegEx(settings), buffer, settings);
    }

    /**
     * Extended constructor for StatementDeParserForRegEx.
     * @param expressionDeParser {@link ExpressionDeParserForRegEx}
     * @param selectDeParser {@link SelectDeParserForRegEx}
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser,
                                     SelectDeParserForRegEx selectDeParser, StringBuilder buffer,
                                     SettingsContainer settings) {
        super(expressionDeParser, selectDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = selectDeParser;
        this.keywordSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.settings = settings;
    }

    /**
     * Overrides visit method for {@link Select} statements.
     * @param select {@link Select} statement
     */
    @Override
    public void visit(Select select) {
        this.selectDeParserForRegEx.setBuffer(this.buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(this.buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegEx);
        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
            this.buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "WITH"));
            this.buffer.append(REQUIRED_WHITE_SPACE);
            this.buffer.append(this.selectDeParserForRegEx.handleWithItemValueList(select));
        }
        select.getSelectBody().accept(selectDeParserForRegEx);
    }

    /**
     * Overrides visit method for {@link Insert} statements.
     * @param insert {@link Insert} statement
     */
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

    /**
     * Overrides visit method for {@link CreateTable} statements.
     * @param createTable {@link CreateTable} statement
     */
    @Override
    public void visit(CreateTable createTable) {
        CreateTableDeParserForRegEx createTableDeParserForRegEx = new CreateTableDeParserForRegEx(this, buffer, settings);
        createTableDeParserForRegEx.deParse(createTable);
    }

    /**
     * Overrides visit method for {@link Update} statements.
     * @param update {@link Update} statement
     */
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

    /**
     * Overrides visit method for {@link Delete} statements.
     * @param delete {@link Delete} statement
     */
    @Override
    public void visit(Delete delete) {
        this.selectDeParserForRegEx.setBuffer(buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(this.expressionDeParserForRegEx);
        DeleteDeParserForRegEx deleteDeParserForRegEx = new DeleteDeParserForRegEx(this.settings, this.expressionDeParserForRegEx, buffer);
        deleteDeParserForRegEx.deParse(delete);
    }

    /**
     * Overrides visit method for {@link Drop} statements.
     * @param drop {@link Drop} statement
     */
    @Override
    public void visit(Drop drop) {
        DropDeParserForRegEx dropDeParserForRegEx = new DropDeParserForRegEx(buffer, this.settings);
        dropDeParserForRegEx.deParse(drop);
    }

    /**
     * Overrides visit method for {@link Statements}.
     * @param stmts {@link Statements}
     */
    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }
}
