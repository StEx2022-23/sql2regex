package sqltoregex.visitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.*;

import java.util.LinkedList;
import java.util.List;

/**
 * StatementVisitor which allows to translate JOIN-ON → WHERE-Expressions.
 * @author Maximilian Förster
 */
public class StatementVisitorJoinToWhere extends StatementVisitorAdapter {
    SelectVisitor selectVisitorJoinToWhere = new SelectVisitorJoinToWhere();

    /**
     * Overrides the default visit(Select select) method to accept these new Visitor.
     * @param select Select statement
     */
    @Override
    public void visit(Select select) {
        select.getSelectBody().accept(selectVisitorJoinToWhere);
    }

    /**
     * Visitor for transforming JOIN-ON-expressions into WHERE-expressions. Deletes the JOIN-ON-expressions after
     * transformation.
     */
    private class SelectVisitorJoinToWhere extends SelectVisitorAdapter {
        @Override
        public void visit(PlainSelect plainSelect) {
            List<Join> joinList = plainSelect.getJoins();

            if (joinList == null) {
                return;
            }
            for (Join join : joinList) {
                for (Expression expression : join.getOnExpressions()){
                    if (plainSelect.getWhere() == null) {
                        plainSelect.setWhere(expression);
                    }else{
                        plainSelect.setWhere(new AndExpression().withLeftExpression(plainSelect.getWhere())
                                                     .withRightExpression(expression));
                    }
                }
                join.setOnExpressions(new LinkedList<>());
            }
        }
    }
}
