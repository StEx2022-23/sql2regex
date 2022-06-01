package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.List;
import java.util.stream.Stream;

class SelectDeParserForRegExTest{
    StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
    @Test
    void testSelectFromWithTwoColumns() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELECT col2,col1 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELCT col2,col1 FROM table1",
                "SELECT col2, col1 FOM table1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1, col2 FROM table1", toCheckedInput, true);
    }

    @Test
    void testSelectFromWithTwoColumnsFailings() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECTcol1, col2 FROM table1",
                "SELECT col2, col1FROM table1",
                "SELECT col2,col1 FROMtable1",
                "SELECT col2 col1 FROM table1",
                "SELCTcol2,col1 FROM table1",
                "SELECTcol2,col1FOMtable1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1, col2 FROM table1", toCheckedInput, false);
    }

    @Test
    void testSimpleInnerJoin() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer, "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2", toCheckedInput, true);
    }

    @Test
    void testFrom() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateOne() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.withStringSynonymGenerator(stringSynonymGenerator);
        List<String> toCheckedInput = Stream.of(
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1, table2",
                "SELECT MITTELWERT(co1) AS c1 FROM table1, table2"
        ).toList();
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT AVG(col1) AS c1 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testAliasAndAggregateTwo() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.withStringSynonymGenerator(stringSynonymGenerator);
        List<String> toCheckedInput = List.of(
                "SELECT AVG(col1) AS c1, column2 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1, column2 FROM table1, table2",
                "SELECT column2, MITTELWERT(co1) AS c1 FROM table2, table1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT AVG(col1) AS c1, column2 FROM table1, table2", toCheckedInput, true);
    }

    @Test
    void testTableAlias() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key", toCheckedInput, true);
    }

    @Test
    void testOptionalAliasAddedByStudent() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1, col2, col3 AS c3", toCheckedInput, true);
    }

    @Test
    void testDistinctKeyword() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT DISTINCT col1",
                "SELECT DISTICT col1",
                "SELECT  DISTINCT  col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT DISTINCT col1", toCheckedInput, true);
    }

    @Test
    void testUniqueKeyword() throws JSQLParserException{
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT UNIQUE col1",
                "SELECT UNIUE col1",
                "SELECT  UNIQUE  col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT UNIQUE col1", toCheckedInput, true);
    }

    @Test
    void testSelectAll() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1",
                "SELECT  *  FROM table1",
                "SELECT ALL FROM table1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table1", toCheckedInput, true);
    }

    @Test
    void testSelectAllTableColumns() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT table1.* FROM table1",
                "SELECT  table1.*  FROM table1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT table1.* FROM table1", toCheckedInput, true);
    }

    @Test
    void testEmitChanges() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 EMIT CHANGES",
                "SELECT * FROM table1  EMIT  CHANGES",
                "SELECT * FROM table1 EMT CHANES"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table1 EMIT CHANGES", toCheckedInput, true);
    }

    @Test
    void testSubSelect() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.withStringSynonymGenerator(stringSynonymGenerator);
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)",
                "SELECT * FROM table1 WHERE (SELECT AVG(col1) AS avgcol1 FROM table2) = col1",
                "SELECT * FROM table1 WHERE (SELECT MITTELWERT(col1) AS avgcol1 FROM table2) = col1"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)", toCheckedInput, true);
    }

    @Test
    void testTableNameAlias() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table1 t1", toCheckedInput, true);
    }

    @Test
    void testTableNameAliasOptionalAddedByStudent() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table1", toCheckedInput, true);
    }

    @Test
    void unPivotStatement() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))"
        );

        String input = "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))";
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,input, toCheckedInput, true);
    }

    @Test
    void fetchStatement() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT * FROM table FETCH NEXT 1 ROWS ONLY",
                "SELECT * FROM table  FTCH  NEXT  1  ROWS  ONLY"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT * FROM table FETCH NEXT 1 ROWS ONLY", toCheckedInput, true);
    }

    @Test
    void testComplexTableNameAliasUse() throws JSQLParserException {
        SettingsContainer defaultSettingsContainer = new SettingsContainer().withAllSpellingMistakesAndOrderRotations();
        List<String> toCheckedInput = List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE tab1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 AS t1 WHERE tab1.c1 = 5"
        );
        TestUtils.validateListAgainstRegEx(defaultSettingsContainer,"SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5", toCheckedInput, true);
    }
}
