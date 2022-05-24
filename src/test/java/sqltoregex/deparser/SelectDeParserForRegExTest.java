package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SelectDeParserForRegExTest {
    StringBuilder buffer = new StringBuilder();
    StatementDeParser statementDeParser = new StatementDeParserForRegEx(new ExpressionDeParserForRegEx(new SettingsManager()), buffer);

    SelectDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }

    boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    String getRegEx(String sampleSolution) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        System.out.println(statementDeParser.getBuffer().toString());
        return statementDeParser.getBuffer().toString();
    }

    void validateListAgainstRegEx(String sampleSolution, List<String> alternativeStatements, boolean isAssertTrue) throws JSQLParserException {
        String regex = this.getRegEx(sampleSolution);
        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

    @Test
    void testSelectFromWithTwoColumns() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELECT col2,col1 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELCT col2,col1 FROM table1",
                "SELECT col2, col1 FOM table1"
        );
        validateListAgainstRegEx("SELECT col1, col2 FROM table1", toCheckedInput, true);
    }

    @Test
    void testSelectFromWithTwoColumnsFailings() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECTcol1, col2 FROM table1",
                "SELECT col2, col1FROM table1",
                "SELECT col2,col1 FROMtable1",
                "SELECT col2 col1 FROM table1",
                "SELCTcol2,col1 FROM table1",
                "SELECTcol2,col1FOMtable1"
        );
        validateListAgainstRegEx("SELECT col1, col2 FROM table1", toCheckedInput, false);
    }

    @Test
    void testSimpleInnerJoin() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1"
        );
        validateListAgainstRegEx("SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2", toCheckedInput, true);
    }

    @Test
    void testFrom() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        );
        validateListAgainstRegEx("SELECT col1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateOne() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1, table2",
                "SELECT MITTELWERT(co1) AS c1 FROM table1, table2"
        );
        validateListAgainstRegEx("SELECT AVG(col1) AS c1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateTwo() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1, column2 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1, column2 FROM table1, table2",
                "SELECT column2, MITTELWERT(co1) AS c1 FROM table2, table1"
        );
        validateListAgainstRegEx("SELECT AVG(col1) AS c1, column2 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testTableAlias() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key"
        );
        validateListAgainstRegEx("SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key", toCheckedInput, true);
    }

    @Test
    void testOptionalAliasAddedByStudent() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        );
        validateListAgainstRegEx("SELECT col1, col2, col3 AS c3", toCheckedInput, true);
    }

    @Test
    void testDistinctKeyword() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT DISTINCT col1",
                "SELECT DISTICT col1",
                "SELECT  DISTINCT  col1"
        );
        validateListAgainstRegEx("SELECT DISTINCT col1", toCheckedInput, true);
    }

    @Test
    void testUniqueKeyword() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT UNIQUE col1",
                "SELECT UNIUE col1",
                "SELECT  UNIQUE  col1"
        );
        validateListAgainstRegEx("SELECT UNIQUE col1", toCheckedInput, true);
    }

    @Test
    void testSelectAll() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1",
                "SELECT  *  FROM table1",
                "SELECT ALL FROM table1"
        );
        validateListAgainstRegEx("SELECT * FROM table1", toCheckedInput, true);
    }

    @Test
    void testSelectAllTableColumns() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT table1.* FROM table1",
                "SELECT  table1.*  FROM table1"
        );
        validateListAgainstRegEx("SELECT table1.* FROM table1", toCheckedInput, true);
    }

    @Test
    void testEmitChanges() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 EMIT CHANGES",
                "SELECT * FROM table1  EMIT  CHANGES",
                "SELECT * FROM table1 EMT CHANES"
        );
        validateListAgainstRegEx("SELECT * FROM table1 EMIT CHANGES", toCheckedInput, true);
    }

    @Test
    void testSubSelect() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)",
                "SELECT * FROM table1 WHERE (SELECT AVG(col1) AS avgcol1 FROM table2) = col1",
                "SELECT * FROM table1 WHERE (SELECT MITTELWERT(col1) AS avgcol1 FROM table2) = col1"
        );
        validateListAgainstRegEx("SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)", toCheckedInput, true);
    }

    @Test
    void testTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        validateListAgainstRegEx("SELECT * FROM table1 t1", toCheckedInput, true);
    }

    @Test
    void testTableNameAliasOptionalAddedByStudent() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        validateListAgainstRegEx("SELECT * FROM table1", toCheckedInput, true);
    }


    @Test
    void pivotStatement() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM (SELECT c1, p1, q FROM p) PIVOT (SUM(q) as q for p1 in ('a','b')) ORDER BY c1"
        );


        StringBuilder input = new StringBuilder();
        input.append("SELECT *");
        input.append("FROM (SELECT c1, p1, q FROM p)");
        input.append("PIVOT (SUM(q) as q for p1 in ('a','b'))");
        input.append("ORDER BY c1");


        validateListAgainstRegEx(input.toString(), toCheckedInput, false);
    }

}
