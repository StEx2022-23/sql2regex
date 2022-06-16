package sqltoregex.deparser;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import sqltoregex.settings.*;

import java.util.*;

class CreateTableTest {

    @Test
    void createTable1colAndDataType() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 datatype1)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 datatype1)", matchingMap, true);
    }

    @Test
    void createTable1colAndDataTypeAndDefinition() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (column1 datatype1  NOT   NULL)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (column1 datatype1 NOT NULL)", matchingMap, true);
    }

    @Test
    void allWorkingColumnDefinitions() {
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (column1 datatype1 NOT NULL)",
                "CREATE TABLE table1 (column1 datatype1 DEFAULT 5)",
                "CREATE TABLE table1 (column1 datatype1 VISIBLE)",
                "CREATE TABLE table1 (column1 datatype1 INVISIBLE)",
                "CREATE TABLE table1 (column1 datatype1 AUTO_INCREMENT)",
                "CREATE TABLE table1 (column1 datatype1 UNIQUE)",
                "CREATE TABLE table1 (column1 datatype1 UNIQUE KEY)",
                "CREATE TABLE table1 (column1 datatype1 PRIMARY KEY)",
                "CREATE TABLE table1 (column1 datatype1 COMMENT 'string')",
                "CREATE TABLE table1 (column1 datatype1 COLLATE collation_name)",
                "CREATE TABLE table1 (column1 datatype1 COLUMN_FORMAT FIXED)",
                "CREATE TABLE table1 (column1 datatype1 COLUMN_FORMAT DYNAMIC)",
                "CREATE TABLE table1 (column1 datatype1 COLUMN_FORMAT DEFAULT)",
                "CREATE TABLE table1 (column1 datatype1 ENGINE_ATTRIBUTE = 'string')",
                "CREATE TABLE table1 (column1 datatype1 SECONDARY_ENGINE_ATTRIBUTE 'string')",
                "CREATE TABLE table1 (column1 datatype1 STORAGE DISK)",
                "CREATE TABLE table1 (column1 datatype1 STORAGE MEMORY)",
                "CREATE TABLE table1 (column1 datatype1 REFERENCES tbl_name (col1))",
                "CREATE TABLE table1 (column1 datatype1 CHECK (expr))",
                "CREATE TABLE table1 (column1 datatype1 CHECK (expr) ENFORCED)",
                "CREATE TABLE table1 (column1 datatype1 CHECK (expr) NOT ENFORCED)",
                "CREATE TABLE table1 (column1 datatype1 CONSTRAINT symbol CHECK (expr) NOT ENFORCED)",
// 2nd datatype section
                "CREATE TABLE table1 (column1 datatype1 COLLATE collation_name)",
                "CREATE TABLE table1 (column1 datatype1 VIRTUAL NULL)",
                "CREATE TABLE table1 (column1 datatype1 STORED NOT NULL)",
                "CREATE TABLE table1 (column1 datatype1 VISIBLE)",
                "CREATE TABLE table1 (column1 datatype1 INVISIBLE)",
                "CREATE TABLE table1 (column1 datatype1 UNIQUE KEY)",
                "CREATE TABLE table1 (column1 datatype1 KEY)",
                "CREATE TABLE table1 (column1 datatype1 PRIMARY KEY)",
                "CREATE TABLE table1 (column1 datatype1 COMMENT 'string')",
                "CREATE TABLE table1 (column1 datatype1 COMMENT 'string')",
                "CREATE TABLE table1 (column1 datatype1 REFERENCES tbl_name (col1))",
                "CREATE TABLE table1 (column1 datatype1 CONSTRAINT symbol CHECK (expr) NOT ENFORCED)"

        ));
        validateDefaults(toTestList);
    }

    /**
     * All of this are not working cause of only string support for column definitions bz the parser and therefore
     * impossible distinction between regular expression parenthesis and expression parenthesis
     */
    @Test
    void allNotWorkingColumnDefinitions(){
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (column1 datatype1 DEFAULT expr())",
                "CREATE TABLE table1 (column1 datatype1 AS expr())",
                "CREATE TABLE table1 (column1 datatype1 GENERATED ALWAYS AS expr())"
        ));
        Assert.assertThrows(AssertionFailedError.class, () -> validateDefaults(toTestList));
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

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TEMPORARY TABLE table1 (column1 datatype1) AS SELECT col2 FROM table2", matchingMap, true);
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
    void withSingleColumnAndSimpleIndex() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))"
        ));
        matchingMap.put(SettingsOption.OTHERSYNONYMS, List.of(
                "CREATE TABLE table1 (col1 type1, KEY index_name (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))", matchingMap, true);
        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().withStringSet(Set.of("INDEX", "KEY"), SettingsOption.OTHERSYNONYMS).build(), "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))", matchingMap, true);
    }

    @Test
    void withSimpleIndexAndType() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name USING BTREE (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, INDEX index_name USING BTREE (col1))", matchingMap, true);

        Map<SettingsOption, List<String>> matchingMapHash = new EnumMap<>(SettingsOption.class);
        matchingMapHash.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name USING HASH (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, INDEX index_name USING HASH (col1))", matchingMapHash, true);
    }

    /**
     * This test is supposed to fail with the current JSQLParser Version, but included to indicate the situation, when it will be supportet in the parser.
     */
    @Test
    void withSimpleIndexAndOption() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name (col1) KEY BLOCK SIZE 3)"
        ));
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        Assert.assertThrows(AssertionFailedError.class, () -> TestUtils.validateStatementAgainstRegEx(settingsContainer, "CREATE TABLE table1 (col1 type1, INDEX index_name (col1) KEY BLOCK SIZE 3)", matchingMap, true));
    }

    @Test
    void withFullTextSpatial() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, FULLTEXT index_name (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, FULLTEXT index_name (col1))", matchingMap, true);

        Map<SettingsOption, List<String>> matchingMapSpatial = new EnumMap<>(SettingsOption.class);
        matchingMapSpatial.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, SPATIAL index_name (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, SPATIAL index_name (col1))", matchingMapSpatial, true);
    }

    @Test
    void withConstraintPrimaryKey() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))", matchingMap, true);
    }

    @Test
    void withConstraintUnique() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, UNIQUE KEY index_name (col1))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, UNIQUE KEY index_name (col1))", matchingMap, true);
    }

    @Test
    void withConstraintForeignKey() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2))",
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 ( col1 ,col2 ))"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2))", matchingMap, true);
    }

    /**
     * first tests with MATCH term are expected to fail due to parser limitations
     */
    @Test
    void withConstraintForeignKeyWithReferentialActions() {
        SettingsContainer defaultSettingsContainer = SettingsContainer.builder().build();
        Map<SettingsOption, List<String>> matchingMap1 = new EnumMap<>(SettingsOption.class);
        matchingMap1.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH FULL)"
        ));

        Assert.assertThrows(AssertionFailedError.class, () -> TestUtils.validateStatementAgainstRegEx(defaultSettingsContainer, "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH FULL)", matchingMap1, true));

        Map<SettingsOption, List<String>> matchingMap2 = new EnumMap<>(SettingsOption.class);
        matchingMap2.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH PARTIAL)"
        ));

        Assert.assertThrows(AssertionFailedError.class, () -> TestUtils.validateStatementAgainstRegEx(defaultSettingsContainer, "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH PARTIAL)", matchingMap2, true));

        Map<SettingsOption, List<String>> matchingMap3 = new EnumMap<>(SettingsOption.class);
        matchingMap3.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH SIMPLE)"
        ));

        Assert.assertThrows(AssertionFailedError.class, () -> TestUtils.validateStatementAgainstRegEx(defaultSettingsContainer, "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) MATCH SIMPLE)", matchingMap3, true));

        Map<SettingsOption, List<String>> matchingMap4 = new EnumMap<>(SettingsOption.class);
        matchingMap4.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON DELETE CASCADE)",
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON  DELETE  CASCADE)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON DELETE CASCADE)", matchingMap4, true);

        Map<SettingsOption, List<String>> matchingMap5 = new EnumMap<>(SettingsOption.class);
        matchingMap5.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE SET NULL)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE SET NULL)", matchingMap5, true);

        Map<SettingsOption, List<String>> matchingMapRestrict = new EnumMap<>(SettingsOption.class);
        matchingMapRestrict.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE RESTRICT)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE RESTRICT)", matchingMapRestrict, true);

        Map<SettingsOption, List<String>> matchingMapNoAction = new EnumMap<>(SettingsOption.class);
        matchingMapNoAction.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE NO ACTION)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE NO ACTION)", matchingMapNoAction, true);

        Map<SettingsOption, List<String>> matchingMapSetDefault = new EnumMap<>(SettingsOption.class);
        matchingMapSetDefault.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE SET DEFAULT)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1, CONSTRAINT symbol FOREIGN KEY (col1) REFERENCES table2 (col1, col2) ON UPDATE SET DEFAULT)", matchingMapSetDefault, true);
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
    void allWorkingMySqlTableOptions(){
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (col1 type1) AUTOEXTEND_SIZE 1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1",
                "CREATE TABLE table1 (col1 type1) CHECKSUM  1",
                "CREATE TABLE table1 (col1 type1) DEFAULT COLLATE  UTF_8",
                "CREATE TABLE table1 (col1 type1) COLLATE  UTF_8",
                "CREATE TABLE table1 (col1 type1) COMMENT 'comment'",
                "CREATE TABLE table1 (col1 type1) COMPRESSION 'ZLIB'",
                "CREATE TABLE table1 (col1 type1) CONNECTION  'connect_string'",
                "CREATE TABLE table1 (col1 type1) DATA DIRECTORY 'path'",
                "CREATE TABLE table1 (col1 type1) DELAY_KEY_WRITE 0",
                "CREATE TABLE table1 (col1 type1) ENCRYPTION 'Y'",
                "CREATE TABLE table1 (col1 type1) ENGINE engine_name",
                "CREATE TABLE table1 (col1 type1) ENGINE_ATTRIBUTE 'string'",
                "CREATE TABLE table1 (col1 type1) KEY_BLOCK_SIZE 1",
                "CREATE TABLE table1 (col1 type1) MAX_ROWS 1",
                "CREATE TABLE table1 (col1 type1) MIN_ROWS 1",
                "CREATE TABLE table1 (col1 type1) PACK_KEYS 0",
                "CREATE TABLE table1 (col1 type1) PACK_KEYS 1",
                "CREATE TABLE table1 (col1 type1) PASSWORD = 'string'",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = DEFAULT",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = DYNAMIC",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = FIXED",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = COMPRESSED",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = REDUNDANT",
                "CREATE TABLE table1 (col1 type1) ROW_FORMAT = COMPACT",
                "CREATE TABLE table1 (col1 type1) SECONDARY_ENGINE_ATTRIBUTE 'string'",
                "CREATE TABLE table1 (col1 type1) STATS_AUTO_RECALC DEFAULT",
                "CREATE TABLE table1 (col1 type1) STATS_AUTO_RECALC 0",
                "CREATE TABLE table1 (col1 type1) STATS_PERSISTENT  DEFAULT",
                "CREATE TABLE table1 (col1 type1) STATS_PERSISTENT  0",
                "CREATE TABLE table1 (col1 type1) STATS_SAMPLE_PAGES  1",
                "CREATE TABLE table1 (col1 type1) TABLESPACE tablespace_name",
                "CREATE TABLE table1 (col1 type1) TABLESPACE tablespace_name STORAGE DISK",
                "CREATE TABLE table1 (col1 type1) TABLESPACE tablespace_name STORAGE MEMORY"

        ));
        validateDefaults(toTestList);
    }

    /**
     * Tests are expected to fail due to parser limitations. Tests for determining further support by the deparser.
     */
    @Test
    void allNotWorkingMySqlTableOptions(){
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (col1 type1) DEFAULT CHARACTER SET  1",
                "CREATE TABLE table1 (col1 type1) CHARACTER SET  UTF_8",
                "CREATE TABLE table1 (col1 type1) INDEX DIRECTORY 'path'",
                "CREATE TABLE table1 (col1 type1) INSERT_METHOD NO",
                "CREATE TABLE table1 (col1 type1) INSERT_METHOD FIRST",
                "CREATE TABLE table1 (col1 type1) INSERT_METHOD LAST",
                "CREATE TABLE table1 (col1 type1) UNION  tbl_name"

        ));
        Assert.assertThrows(AssertionFailedError.class, () -> validateDefaults(toTestList));
    }

    @Test
    void withOnePartitionOption(){
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)"
        ));

        TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)", matchingMap, true);
    }


    @Test
    void allWorkingMySqlPartitionOptions() {
        List<String> toTestList = new LinkedList<>(List.of(

                "CREATE TABLE table1 (col1 type1) PARTITION BY LINEAR KEY (col1)",
                "CREATE TABLE table1 (col1 type1) PARTITIONS 2",
//subpartitions
                "CREATE TABLE table1 (col1 type1) SUBPARTITION BY LINEAR KEY (col1)",
                "CREATE TABLE table1 (col1 type1) SUBPARTITIONS 2"
        ));
        validateDefaults(toTestList);
    }

    /**
     * Tests are expected to fail due to parser limitations. Tests for determining further support by the deparser.
     */
    @Test
    void allNotWorkingMySqlPartitionOptions() {
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY LINEAR HASH (YEAR(col1))",
                "CREATE TABLE table1 (col1 type1) PARTITION BY LINEAR KEY ALGORITHM=1 (col1)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY RANGE (expr)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY RANGE COLUMNS(col1)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY LIST (YEAR(col1))",
                "CREATE TABLE table1 (col1 type1) PARTITION BY LIST COLUMNS(col1)",
                "CREATE TABLE table1 (col1 type1) SUBPARTITION BY LINEAR HASH (YEAR(col1))",
                "CREATE TABLE table1 (col1 type1) SUBPARTITION BY LINEAR KEY ALGORITHM=1 (col1)"
        ));
        Assert.assertThrows(AssertionFailedError.class, () -> validateDefaults(toTestList));
    }

    /**
     * Tests are expected to fail due to parser limitations. Tests for determining further support by the deparser.
     */
    @Test
    void allNotWorkingMySqlPartitionDefinitions() {
        List<String> toTestList = new LinkedList<>(List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName VALUES LESS THAN (expr())",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName VALUES LESS THAN (5,1)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName VALUES MAXVALUE)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT = 'String')",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName IN (5,4,2))",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName ENGINE engine_name)",
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT 'string')"
        ));
        Assert.assertThrows(AssertionFailedError.class, () -> validateDefaults(toTestList));
    }



    void validateDefaults(List<String> toTestList){
        for(String toTest : toTestList){
            Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
            matchingMap.put(SettingsOption.DEFAULT, List.of(
                    toTest
            ));
            TestUtils.validateStatementAgainstRegEx(SettingsContainer.builder().build(), toTest, matchingMap, true);
        }
    }
}
