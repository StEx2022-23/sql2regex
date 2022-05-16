package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.visitor.SelectVisitorJoinToWhere;

public class ExpressionDeParserForRegExTest {

    @Test
    void commutativeBinaryExpression() throws JSQLParserException {
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "1 AND 2");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx();
        deParser.setBuffer(b);
        expression.accept(deParser);
        Assertions.assertEquals("1\\s+AND\\s+2|2\\s+AND\\s+1", b.toString());
    }

    @Test
    void between() throws JSQLParserException {
        Expression expression = (Expression) CCJSqlParserUtil.parseExpression(
                "a BETWEEN 1 AND 5");
        StringBuilder b = new StringBuilder();
        ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx();
        deParser.setBuffer(b);
        expression.accept(deParser);
        Assertions.assertEquals("a\\s+BETWEEN\\s+1\\s+AND\\s+5", b.toString());
    }
}
