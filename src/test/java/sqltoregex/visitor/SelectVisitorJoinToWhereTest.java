package sqltoregex.visitor;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.visitor.StatementVisitorJoinToWhere;

import static org.junit.jupiter.api.Assertions.fail;

class SelectVisitorJoinToWhereTest {

    void visitAndCheckAgainst(String statement, String toCheck) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(
                    statement);
            select.accept(new StatementVisitorJoinToWhere());

            Assertions.assertEquals(toCheck,
                                    select.toString());
        }catch(JSQLParserException e){
            fail("Couldn't parse statement" + e);
        }
    }

    @Test
    void emptyJoin() {
        visitAndCheckAgainst("SELECT col1 FROM table1, table2 WHERE (col1 = col2) AND col3 = col4","SELECT col1 FROM table1, table2 WHERE (col1 = col2) AND col3 = col4");
    }

    @Test
    void emptyWhere() {
        visitAndCheckAgainst("SELECT col1 FROM table1 INNER JOIN table2 ON (col1=col2) AND col3 = col4","SELECT col1 FROM table1 INNER JOIN table2 WHERE (col1 = col2) AND col3 = col4");
    }

    @Test
    void emptyJoinAndWhere() {
        visitAndCheckAgainst("SELECT col1 FROM table1 INNER JOIN table2","SELECT col1 FROM table1 INNER JOIN table2");
    }

    @Test
    void populatedWhere() {
        visitAndCheckAgainst("SELECT col1 FROM table1 INNER JOIN table2 ON (col1=col2) AND col3 = col4 WHERE col1=5","SELECT col1 FROM table1 INNER JOIN table2 WHERE col1 = 5 AND (col1 = col2) AND col3 = col4");
    }

    /**
     * added support according to https://github.com/JSQLParser/JSqlParser/issues/1302
     */
    @Test
    void multipleOnExpressions() {
        visitAndCheckAgainst("SELECT col1 FROM table1 INNER JOIN table2 INNER JOIN table3 ON col1=col2 ON col3=col4","SELECT col1 FROM table1 INNER JOIN table2 INNER JOIN table3 WHERE col1 = col2 AND col3 = col4");
    }

}
