package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

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

    boolean checkAgainstRegEx(String regex, String toChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toChecked);
        return matcher.matches();
    }

    @Test
    void testSelectFromWithTwoColumns() throws JSQLParserException {
        String sampleSolution = "SELECT col1, col2 FROM table1";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELECT col2,col1 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELCT col2,col1 FROM table1",
                "SELECT col2, col1 FOM table1"
        );

        for(String str : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

    @Test
    void testSelectFromWithTwoColumnsFailings() throws JSQLParserException {
        String sampleSolution = "SELECT col1, col2 FROM table1";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        List<String> toCheckedInput = List.of(
                "SELECTcol1, col2 FROM table1",
                "SELECT col2, col1FROM table1",
                "SELECT col2,col1 FROMtable1",
                "SELECT col2 col1 FROM table1",
                "SELCTcol2,col1 FROM table1",
                "SELECTcol2,col1FOMtable1"
        );

        for(String str : toCheckedInput){
            Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

    @Test
    void testGroupByTwoStatements() throws JSQLParserException {
        String sampleSolution = "SELECT col1 GROUP BY col1, col2";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELCT col1 GROUP BY col1, col2",
                "SELECT col1 GROUP BY col2,col1",
                "SELECT col1 GROUP BY col1 , col2",
                "SELECT col1 GROUP BY col2,col1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testGroupByThreeStatements() throws JSQLParserException {
        String sampleSolution = "SELECT col1 GROUP BY col1, col2, col3";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1, col2, col3",
                "SELECT col1 GROUP BY col2,col1, col3",
                "SELECT col1 GROUP BY col3, col1, col2",
                "SELECT col1 GROUP BY col3, col2, col1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testGroupByThreeStatementsFailings() throws JSQLParserException {
        String sampleSolution = "SELECT col1 GROUP BY col1, col2, col3";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 GROUP BY col1 col2, col3",
                "SELECT col1 GROUPBY col2,col1, col3",
                "SELECT col1 GROUP BYcol3, col1, col2",
                "SELECT col1 GROUP BY col3col2col1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertFalse(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testSimpleInnerJoin() throws JSQLParserException {
        String sampleSolution = "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testLimit() throws JSQLParserException {
        String sampleSolution = "SELECT col1 LIMIT 2, 3";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 LIMT 3 OFFSET 2",
                "SELECT col1 LIMIT 3 OFFST 2",
                "SELECT col1 LIMIT    3   OFFSET    2"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testOffset() throws JSQLParserException {
        String sampleSolution = "SELECT col1 OFFSET 10 ROWS";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 OFFSET 10 ROWS",
                "SELECT col1 OFFSET  10  ROWS",
                "SELECT col1 OFFST 10 RWS"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testFrom() throws JSQLParserException {
        String sampleSolution = "SELECT col1 FROM table1, table2";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testAliasAndAggregateOne() throws JSQLParserException{
        String sampleSolution = "SELECT AVG(col1) AS c1 FROM table1, table2";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1, table2",
                "SELECT MITTELWERT(co1) AS c1 FROM table1, table2"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testAliasAndAggregateTwo() throws JSQLParserException{
        String sampleSolution = "SELECT AVG(col1) AS c1, column2 FROM table1, table2";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1, column2 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1, column2 FROM table1, table2",
                "SELECT column2, MITTELWERT(co1) AS c1 FROM table2, table1"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testTableAlias() throws JSQLParserException{
        String sampleSolution = "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }

    @Test
    void testOptionalAliasAddedByStudent() throws JSQLParserException{
        String sampleSolution = "SELECT col1, col2, col3 AS c3";

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParser);
        String regex = statementDeParser.getBuffer().toString();

        System.out.println(regex);

        List<String> toCheckedInput = List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        );

        for(String sql : toCheckedInput){
            Assertions.assertTrue(checkAgainstRegEx(regex, sql), sql + " " + regex);
        }
    }
}
