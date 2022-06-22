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
    void testInsertStatementsWithQuotationMarks(){
        final String sampleSolution = "SELECT `col1`, `col2` FROM `table1`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT 'col1', \"col2\" FROM `table1´"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }


    @Test
    void testSelectFromWithTwoColumns()  {
        final String sampleSolution = "SELECT `col1`, `col2` FROM `table1`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        Map<SettingsOption, List<String>> nonMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT \"col1\", 'col2' FROM 'table1'",
                "SELECT   col1  , col2  FROM  table1"
        ));
        nonMatchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECTcol1, col2 FROM table1",
                "SELECT   col1  , col2  FROMtable1"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col1, col2 FROM table1",
                "SELECT col2, col1 FROM table1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELCT col1, col2 FRM table1"
        ));
        nonMatchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SECT col1, col2 FM table1"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMESPELLING, List.of(
                "SELECT cl1, cl2 FROM table1"
        ));
        nonMatchingMap.put(SettingsOption.COLUMNNAMESPELLING, List.of(
                "SELECT c1, c2 FROM table1"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "SELECT col1, col2 FROM tabe1"
        ));
        nonMatchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "SELECT cl1, cl2 FROM tab1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                nonMatchingMap,
                false
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMESPELLING).build(),
                sampleSolution,
                nonMatchingMap,
                false
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                nonMatchingMap,
                false
        );
    }

    @Test
    void testSimpleInnerJoin()  {
        final String sampleSolution = "SELECT `col1` FROM `table1` INNER JOIN `table2` ON `col1` = `col2`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1 INNER JOIN table2 ON col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col1 = col2",
                "SELECT col1 FROM table1 INNER  JOIN  table2  ON  col2 = col1",
                "SELECT 'col1' FROM 'table1' INNER  JOIN  'table2'  ON  col2 = col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testFrom()  {
        final String sampleSolution = "SELECT `col1` FROM `table1`, `table2`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        Map<SettingsOption, List<String>> nonMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM  table1   ,  table2",
                "SELECT col1 FROM  table1,table2",
                "SELECT 'col1' FROM 'table1', 'table2'"
        ));
        matchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "SELECT col1 FROM table1, table2",
                "SELECT col1 FROM table2, table1"
        ));
        nonMatchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "SELECT col1 FROM table1, table1",
                "SELECT col1 FROM table2, table2"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "SELECT col1 FROM tabl1, tabe2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                sampleSolution,
                nonMatchingMap,
                false
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    /**
     * Test for rotating aggregate function isn't needed, cause its handled as a normal column
     */
    @Test
    void testAliasAndAggregateFunction() {
        final String sampleSolution = "SELECT AVG(`col1`) AS `c1` FROM `table1`";
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT AVG(col1) AS c1 FROM table1",
                "SELECT AVG ( col1 ) ALIAS c1 FROM table1",
                "SELECT AVG(col1) AS c1 FROM table1",
                "SELECT AVG('col1') AS 'c1' FROM 'table1'"
        ));
        matchingMap.put(SettingsOption.AGGREGATEFUNCTIONLANG, List.of(
                "SELECT MITTELWERT(col1) AS c1 FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(stringSynonymGenerator).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testTableAlias() {
        final String sampleSolution = "SELECT `col1`, `col2` FROM `table1` `t1` INNER JOIN `table2` `t2` ON `t1`.`key` = `t2`.`key`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key",
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key",
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON table2.key = table1.key",
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON table2.key = t1.key",
                "SELECT col1, col2 FROM table1 t1 INNER JOIN table2 t2 ON t2.key = t1.key",
                "SELECT 'col1', 'col2' FROM 'table1' 't1' INNER JOIN 'table2' 't2' ON 't2'.'key' = 't1'.'key'"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "SELECT col2, col1 FROM table1 t1 INNER JOIN table2 t2 ON t1.key = t2.key"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testOptionalAliasAddedByStudent() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1, col2, col3 AS c3",
                "SELECT col1, col2 AS c2, col3 AS c3",
                "SELECT col1 AS c1, col2, col3 AS c3",
                "SELECT col1 AS c1, col2 AS c2, col3 AS c3"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1, col2, col3 AS c3",
                matchingMap,
                true
        );
    }

    @Test
    void testDistinctKeyword() {
        final String sampleSolution = "SELECT DISTINCT col1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT DISTINCT col1",
                "SELECT  DISTINCT  col1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT DISTICT col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testUniqueKeyword() {
        final String sampleSolution = "SELECT UNIQUE col1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT UNIQUE col1",
                "SELECT  UNIQUE  col1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT UNIUE col1"

        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testSelectAll()  {
        final String sampleSolution = "SELECT * FROM table1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1",
                "SELECT  *  FROM table1",
                "SELECT ALL FROM table1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT AL FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testSelectAllTableColumns()  {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT table1.* FROM table1",
                "SELECT  table1.*  FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT table1.* FROM table1",
                matchingMap,
                true
        );
    }

    @Test
    void testEmitChanges()  {
        final String sampleSolution = "SELECT * FROM table1 EMIT CHANGES";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 EMIT CHANGES",
                "SELECT * FROM table1  EMIT  CHANGES"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT * FROM table1 EMT CHANES"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testSubSelect()  {
        final String sampleSolution = "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)";
        stringSynonymGenerator.addSynonymFor("AVG", "MITTELWERT");
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 WHERE col1 = (SELECT AVG(col1) AS avgcol1 FROM table2)"
        ));
        matchingMap.put(SettingsOption.AGGREGATEFUNCTIONLANG, List.of(
                "SELECT * FROM table1 WHERE (SELECT AVG(col1) AS avgcol1 FROM table2) = col1",
                "SELECT * FROM table1 WHERE (SELECT MITTELWERT(col1) AS avgcol1 FROM table2) = col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder()
                        .build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder()
                        .with(stringSynonymGenerator)
                        .build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testTableNameAlias()  {
        final String sampleSolution = "SELECT * FROM table1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1",
                "SELECT * FROM table1 AS t"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "SELECT * FROM table1 AS t",
                "SELECT * FROM table AS t1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testTableNameAliasOptionalAddedByStudent()  {
        final String sampleSolution = "SELECT * FROM table1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table1 t1",
                "SELECT * FROM table1 AS t1",
                "SELECT * FROM table1 ALIAS t1"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "SELECT * FROM table1 AS t"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void unPivotStatement()  {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))"
        ));
        String input = "SELECT * FROM (SELECT c1, p1) UNPIVOT (q FOR p1 IN ('a','b'))";
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                input,
                matchingMap,
                true
        );
    }

    @Test
    void fetchStatement()  {
        final String sampleSolution = "SELECT * FROM table FETCH NEXT 1 ROWS ONLY";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT * FROM table FETCH NEXT 1 ROWS ONLY"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT * FROM table  FTCH  NEXT  1  ROWS  ONLY"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testComplexTableNameAliasUse()  {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE tab1.c1 = 5",
                "SELECT col1 AS c1 FROM tab1 AS t1 WHERE tab1.c1 = 5"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5",
                matchingMap,
                true
        );
    }


    @Test
    void testHavingWithAlias()  {
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor("SUM", "SUMME");

        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING SUM(t1.id) > 5 && AVG(costs) > 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING AVG(costs) > 5 && SUM(t1.id) > 5"
        ));
        matchingMap.put(SettingsOption.AGGREGATEFUNCTIONLANG, List.of(
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING SUMME(tab1.id) > 5 && AVG(costs) > 5",
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING AVG(costs) > 5 && SUMME(tab1.id) > 5"
        ));

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING SUM(t1.id) > 5 && AVG(costs) > 5",
                matchingMap,
                true
        );

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(stringSynonymGenerator).build(),
                "SELECT col1 AS c1 FROM tab1 t1 WHERE t1.c1 = 5 HAVING SUM(t1.id) > 5 && AVG(costs) > 5",
                matchingMap,
                true
        );
    }

    @Test
    void testQuotationMarks() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT \"col1\", `col2` FROM 'table1'"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1, col2 FROM table1",
                matchingMap,
                true
        );
    }
}
