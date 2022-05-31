package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

class SelectDeParserForRegExTest extends UserSettingsPreparer{
    TestUtils testUtils = new TestUtils();

    SelectDeParserForRegExTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.ALL);
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
        testUtils.validateListAgainstRegEx("SELECT col1, col2 FROM table1", toCheckedInput, true);
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
        testUtils.validateListAgainstRegEx("SELECT col1, col2 FROM table1", toCheckedInput, false);
    }

    @Test
    void testSimpleInnerJoin() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2", toCheckedInput, true);
    }

    @Test
    void testFrom() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateOne() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1, table2",
                "SELECT MITTELWERT(co1) AS c1 FROM table1, table2"
        );
        testUtils.validateListAgainstRegEx("SELECT AVG(col1) AS c1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateTwo() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1, column2 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1, column2 FROM table1, table2",
                "SELECT column2, MITTELWERT(co1) AS c1 FROM table2, table1"
        );
        testUtils.validateListAgainstRegEx("SELECT AVG(col1) AS c1, column2 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testTableAlias() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key"
        );
        testUtils.validateListAgainstRegEx("SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key", toCheckedInput, true);
    }

    @Test
    void testOptionalAliasAddedByStudent() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        );
        testUtils.validateListAgainstRegEx("SELECT col1, col2, col3 AS c3", toCheckedInput, true);
    }

    @Test
    void testDistinctKeyword() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT DISTINCT col1",
                "SELECT DISTICT col1",
                "SELECT  DISTINCT  col1"
        );
        testUtils.validateListAgainstRegEx("SELECT DISTINCT col1", toCheckedInput, true);
    }

    @Test
    void testUniqueKeyword() throws JSQLParserException{
        List<String> toCheckedInput = List.of(
                "SELECT UNIQUE col1",
                "SELECT UNIUE col1",
                "SELECT  UNIQUE  col1"
        );
        testUtils.validateListAgainstRegEx("SELECT UNIQUE col1", toCheckedInput, true);
    }

    @Test
    void testSelectAll() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1",
                "SELECT  *  FROM table1",
                "SELECT ALL FROM table1"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table1", toCheckedInput, true);
    }

    @Test
    void testSelectAllTableColumns() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT table1.* FROM table1",
                "SELECT  table1.*  FROM table1"
        );
        testUtils.validateListAgainstRegEx("SELECT table1.* FROM table1", toCheckedInput, true);
    }

    @Test
    void testEmitChanges() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 EMIT CHANGES",
                "SELECT * FROM table1  EMIT  CHANGES",
                "SELECT * FROM table1 EMT CHANES"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table1 EMIT CHANGES", toCheckedInput, true);
    }

    @Test
    void testSubSelect() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)",
                "SELECT * FROM table1 WHERE (SELECT AVG(col1) AS avgcol1 FROM table2) = col1",
                "SELECT * FROM table1 WHERE (SELECT MITTELWERT(col1) AS avgcol1 FROM table2) = col1"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)", toCheckedInput, true);
    }

    @Test
    void testTableNameAlias() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table1 t1", toCheckedInput, true);
    }

    @Test
    void testTableNameAliasOptionalAddedByStudent() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table1", toCheckedInput, true);
    }

    @Test
    void unPivotStatement() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))"
        );

        String input = "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))";
        testUtils.validateListAgainstRegEx(input, toCheckedInput, true);
    }

    @Test
    void fetchStatement() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table FETCH NEXT 1 ROWS ONLY",
                "SELECT * FROM table  FTCH  NEXT  1  ROWS  ONLY"
        );
        testUtils.validateListAgainstRegEx("SELECT * FROM table FETCH NEXT 1 ROWS ONLY", toCheckedInput, true);
    }

    @Test
    void testComplexTableNameAliasUse() throws JSQLParserException {
        List<String> toCheckedInput = List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE tab1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 AS t1 WHERE tab1.c1 = 5"
        );
        testUtils.validateListAgainstRegEx("SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5", toCheckedInput, true);
    }
}
