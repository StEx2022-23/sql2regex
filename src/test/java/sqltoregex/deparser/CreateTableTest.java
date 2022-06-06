package sqltoregex.deparser;

import org.junit.jupiter.api.Test;
import sqltoregex.settings.*;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class CreateTableTest {

    @Test
    void createTableSimple() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 datatype1)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 datatype1)", matchingMap, true);
    }

    @Test
    void dataTypeSynonym() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 BOOL)"
        ));

        matchingMap.put(SettingsOption.DATATYPESYNONYMS, List.of(
                "CREATE TABLE table1 (column1 BOOLEAN)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 BOOL)", matchingMap, true);
        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder()
                                                        .withStringSet(new HashSet<>(List.of("BOOL", "BOOLEAN")),
                                                                       SettingsOption.DATATYPESYNONYMS)
                                                        .build(), "CREATE TABLE table1 (column1 BOOL)", matchingMap, true);
    }

    @Test
    void createTemporaryAndIfNotExists(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TEMPORARY TABLE IF NOT EXISTS table1 (column1 datatype1)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TEMPORARY TABLE IF NOT EXISTS table1 (column1 datatype1)", matchingMap, true);
    }

    @Test
    void createAs(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TEMPORARY TABLE table1 (column1 datatype1) AS SELECT col2 FROM table2"
        ));

        System.out.println(TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TEMPORARY TABLE table1 (column1 datatype1) AS SELECT col2 FROM table2", matchingMap, true));
    }

    @Test
    void createLIKE(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 datatype1) LIKE old_tbl_name"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 datatype1) LIKE (old_tbl_name)", matchingMap, true);
    }

    @Test
    void withSingleColumnAndIndex() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))", matchingMap, true);
    }

    @Test
    void withMultipleSimpleColumns() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 datatype1, column2 datatype2, column3 datatype3)"
        ));
matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "CREATE TABLE table1 (column2 datatype2, column1 datatype1, column3 datatype3)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 datatype1, column2 datatype2, column3 datatype3)", matchingMap, true);
        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(), "CREATE TABLE table1 (column1 datatype1, column2 datatype2, column3 datatype3)", matchingMap, true);
    }

    @Test
    void withConstraint() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))", matchingMap, true);
    }

    @Test
    void with2TableConstraint() {
        final String sampleSolution = "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1, col2))";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1, col2))"
        ));
        matchingMap.put(SettingsOption.INDEXCOLUMNNAMEORDER, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col2, col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), sampleSolution , matchingMap, true);
        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().with(SettingsOption.INDEXCOLUMNNAMEORDER) .build(), sampleSolution , matchingMap, true);
    }

    @Test
    void withConstraintBetween2Columns() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, col2 type2, CONSTRAINT my_constraint PRIMARY KEY (col1))"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1), col2 type2)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1), col2 type2)", matchingMap, true);
    }

    /**
     * check if decision is to implement:
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1",
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT =1",
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT= 1"
     */
    @Test
    void withOneTableOption(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1", matchingMap, true);
    }

    @Test
    void withOneTableOptionWithEquals(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT =1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT= 1"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1", matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        notMatchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT1"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1", notMatchingMap, false);
    }


    /**
     * , seperated Tableoptions isn't supported yet by deparser
     */
    @Test
    void withMultipleTableOptions(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1 COMMENT = 'comment'"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1 COMMENT = 'comment'", matchingMap, true);
    }

    @Test
    void withOnePartitionOption(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)", matchingMap, true);
    }

    /**
     *  Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
     *                 "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT =
     *                 'String')"
     *         ));
     *
     *         validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName
     *         VALUES IN (val1, val2))", alternativeStatements, true);
     */
    @Test
    void withOnePartitionOptionAndOnePartitionDefinition(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
matchingMap.put(SettingsOption.DEFAULT, List.of(
                //"CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT = 'String')"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)", matchingMap, true);
    }
}
