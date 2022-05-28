package sqltoregex.visitor;

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Visitor for transforming JOIN-ON-expressions into WHERE-expressions. Deletes the JOIN-ON-expressions after
 * transformation.
 */
public class SelectVisitorJoinToWhere extends SelectVisitorAdapter {
    @Override
    public void visit(PlainSelect plainSelect) {
        List<Join> joinList = plainSelect.getJoins();

        if (joinList == null) {
            return;
        }
        for (Join join : joinList) {
            //In MySQL, it is not possible to have multiple ON, all are concatenated using "AND" that's why the
            // OnExpressionList has always 1 Element for our useCase
            if (join.getOnExpressions().stream().findFirst().isEmpty()) {
                continue;
            }
            if (plainSelect.getWhere() == null) {
                if (join.getOnExpressions().stream().findFirst().isEmpty()) {
                    throw new NoSuchElementException();
                }
                plainSelect.setWhere(join.getOnExpressions().stream().findFirst().get());
            } else {
                plainSelect.setWhere(new AndExpression().withLeftExpression(plainSelect.getWhere())
                                             .withRightExpression(join.getOnExpressions().stream()
                                                                          .findFirst().get()));
            }
        }
        for (Join join : joinList) {
            join.setOnExpressions(new LinkedList<>());
        }
    }
}