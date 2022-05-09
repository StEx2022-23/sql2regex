package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.visitor.SelectVisitorJoinToWhere;

import java.util.LinkedList;
import java.util.List;

public class SelectVisitorJoinToWhereTest {

    @Test
    public void emptyWhere() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(
                "SELECT col1 FROM table1 INNER JOIN table2 ON (col1=col2) AND col3 = col4");
        select.getSelectBody().accept(new SelectVisitorJoinToWhere());

        Assertions.assertEquals(select.toString(), "SELECT col1 FROM table1 INNER JOIN table2 WHERE (col1 = col2) AND col3 = col4");
    }

    @Test
    public void populatedWhere() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(
                "SELECT col1 FROM table1 INNER JOIN table2 ON (col1=col2) AND col3 = col4 WHERE col1=5");
        select.getSelectBody().accept(new SelectVisitorJoinToWhere());

        Assertions.assertEquals(select.toString(), "SELECT col1 FROM table1 INNER JOIN table2 WHERE col1 = 5 AND (col1 = col2) AND col3 = col4");
    }

}
