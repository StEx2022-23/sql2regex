package sqltoregex.deparser;

import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class SelectDeParserForRegExTest{
    StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
    @Test
    void testSelectFromWithTwoColumns()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELECT col2,col1 FROM table1",
                "SELECT col2, col1 FROM table1",
                "SELCT col2,col1 FROM table1",
                "SELECT col2, col1 FOM table1"
        ));;
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1, col2 FROM table1",
                matchingMap,
                true
        );
    }

    @Test
    void testSelectFromWithTwoColumnsFailings()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECTcol1, col2 FROM table1",
                "SELECT col2, col1FROM table1",
                "SELECT col2,col1 FROMtable1",
                "SELECT col2 col1 FROM table1",
                "SELCTcol2,col1 FROM table1",
                "SELECTcol2,col1FOMtable1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1, col2 FROM table1",
                matchingMap,
                false
        );
    }

    @Test
    void testSimpleInnerJoin()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                matchingMap,
                true
        );
    }

    @Test
    void testFrom()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1 FROM table1, table2",
                matchingMap,
                true
        );
    }

    @Test
    void testAliasAndAggregateOne() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.with(stringSynonymGenerator);
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1, table2",
                "SELECT MITTELWERT(co1) AS c1 FROM table1, table2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT AVG(col1) AS c1 FROM table1, table2",
                matchingMap,
                true
        );
    }

    @Test
    void testAliasAndAggregateTwo() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.with(stringSynonymGenerator);
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT AVG(col1)); AS c1, column2 FROM table1, table2",
                "SELECT AVG ( col1 ) ALIAS c1, column2 FROM table1, table2",
                "SELECT column2, MITTELWERT(co1) AS c1 FROM table2, table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT AVG(col1) AS c1, column2 FROM table1, table2",
                matchingMap,
                true
        );
    }

    @Test
    void testTableAlias() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                matchingMap,
                true
        );
    }

    @Test
    void testOptionalAliasAddedByStudent() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1, col2, col3 AS c3",
                matchingMap,
                true
        );
    }

    @Test
    void testDistinctKeyword() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT DISTINCT col1",
                "SELECT DISTICT col1",
                "SELECT  DISTINCT  col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT DISTINCT col1",
                matchingMap,
                true
        );
    }

    @Test
    void testUniqueKeyword() {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT UNIQUE col1",
                "SELECT UNIUE col1",
                "SELECT  UNIQUE  col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT UNIQUE col1",
                matchingMap,
                true
        );
    }

    @Test
    void testSelectAll()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1",
                "SELECT  *  FROM table1",
                "SELECT ALL FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table1",
                matchingMap,
                true
        );
    }

    @Test
    void testSelectAllTableColumns()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT table1.* FROM table1",
                "SELECT  table1.*  FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT table1.* FROM table1",
                matchingMap,
                true
        );
    }

    @Test
    void testEmitChanges()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 EMIT CHANGES",
                "SELECT * FROM table1  EMIT  CHANGES",
                "SELECT * FROM table1 EMT CHANES"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table1 EMIT CHANGES",
                matchingMap,
                true
        );
    }

    @Test
    void testSubSelect()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        defaultSettingsContainer.with(stringSynonymGenerator);
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1)); AS avgcol1 FROM table2)",
                "SELECT * FROM table1 WHERE (SELECT AVG(col1) AS avgcol1 FROM table2) = col1",
                "SELECT * FROM table1 WHERE (SELECT MITTELWERT(col1) AS avgcol1 FROM table2) = col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)",
                matchingMap,
                true
        );
    }

    @Test
    void testTableNameAlias()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table1 t1",
                matchingMap,
                true
        );
    }

    @Test
    void testTableNameAliasOptionalAddedByStudent()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table1",
                matchingMap,
                true
        );
    }

    @Test
    void unPivotStatement()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM (SELECT c1, p1)); UNPIVOT (q FOR p1 IN ('a','b'))"
        ));
        String input = "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))";
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                input,
                matchingMap,
                true
        );
    }

    @Test
    void fetchStatement()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table FETCH NEXT 1 ROWS ONLY",
                "SELECT * FROM table  FTCH  NEXT  1  ROWS  ONLY"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT * FROM table FETCH NEXT 1 ROWS ONLY",
                matchingMap,
                true
        );
    }

    @Test
    void testComplexTableNameAliasUse()  {
        SettingsContainer defaultSettingsContainer = TestUtils.getSettingsContainerWithAllSpellingMistakesAndOrderRotations();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE tab1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 AS t1 WHERE tab1.c1 = 5"
        ));
        TestUtils.validateStatementAgainstRegEx(
                defaultSettingsContainer,
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                matchingMap,
                true
        );
    }
}
