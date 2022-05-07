package sqltoregex.deparser;

import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class StatementDeparser extends StatementDeParser {
    public StatementDeparser(StringBuilder buffer) {
        super(buffer);
    }

    public StatementDeparser(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }

    public StatementDeparser(ExpressionDeParser expressionDeParser, StringBuilder buffer) {
        super(expressionDeParser, new SelectDeParser(), buffer);
    }

    @Override
    public void visit(CreateIndex createIndex) {
        super.visit(createIndex);
    }

    @Override
    public void visit(CreateTable createTable) {
        super.visit(createTable);
    }

    @Override
    public void visit(CreateView createView) {
        super.visit(createView);
    }

    @Override
    public void visit(AlterView alterView) {
        super.visit(alterView);
    }

    @Override
    public void visit(Delete delete) {
        super.visit(delete);
    }

    @Override
    public void visit(Drop drop) {
        super.visit(drop);
    }

    @Override
    public void visit(Insert insert) {
        super.visit(insert);
    }

    @Override
    public void visit(Replace replace) {
        super.visit(replace);
    }

    @Override
    public void visit(Select select) {
        super.visit(select);
    }

    @Override
    public void visit(Truncate truncate) {
        super.visit(truncate);
    }

    @Override
    public void visit(Update update) {
        super.visit(update);
    }

    @Override
    public void visit(Alter alter) {
        super.visit(alter);
    }

    @Override
    public void visit(Statements stmts) {
        super.visit(stmts);
    }

    @Override
    public void visit(Execute execute) {
        super.visit(execute);
    }

    @Override
    public void visit(SetStatement set) {
        super.visit(set);
    }

    @Override
    public void visit(ResetStatement reset) {
        super.visit(reset);
    }

    @Override
    public void visit(Merge merge) {
        super.visit(merge);
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
        super.visit(savepointStatement);
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        super.visit(rollbackStatement);
    }

    @Override
    public void visit(Commit commit) {
        super.visit(commit);
    }

    @Override
    public void visit(Upsert upsert) {
        super.visit(upsert);
    }

    @Override
    public void visit(UseStatement use) {
        super.visit(use);
    }

    @Override
    public void visit(ShowColumnsStatement show) {
        super.visit(show);
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        super.visit(showTables);
    }

    @Override
    public void visit(Block block) {
        super.visit(block);
    }

    @Override
    public void visit(Comment comment) {
        super.visit(comment);
    }

    @Override
    public void visit(ValuesStatement values) {
        super.visit(values);
    }

    @Override
    public void visit(DescribeStatement describe) {
        super.visit(describe);
    }

    @Override
    public void visit(ExplainStatement explain) {
        super.visit(explain);
    }

    @Override
    public void visit(ShowStatement show) {
        super.visit(show);
    }

    @Override
    public void visit(DeclareStatement declare) {
        super.visit(declare);
    }

    @Override
    public void visit(Grant grant) {
        super.visit(grant);
    }

    @Override
    public void visit(CreateSchema aThis) {
        super.visit(aThis);
    }

    @Override
    public void visit(CreateSequence createSequence) {
        super.visit(createSequence);
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        super.visit(alterSequence);
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        super.visit(createFunctionalStatement);
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        super.visit(createSynonym);
    }

    @Override
    public void visit(AlterSession alterSession) {
        super.visit(alterSession);
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        super.visit(ifElseStatement);
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        super.visit(renameTableStatement);
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        super.visit(purgeStatement);
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        super.visit(alterSystemStatement);
    }

    @Override
    public StringBuilder getBuffer() {
        return super.getBuffer();
    }

    @Override
    public void setBuffer(StringBuilder buffer) {
        super.setBuffer(buffer);
    }
}
