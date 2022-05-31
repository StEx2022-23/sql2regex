package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsForm;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.UserSettings;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

class CreateTableTest {

    private StringBuilder buffer;
    private final SettingsManager settingsManager = new SettingsManager();
    private CreateTableDeParserForRegEx deParser;

    CreateTableTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, URISyntaxException {
        this.buffer = new StringBuilder();
        settingsManager.parseUserSettingsInput(new SettingsForm(null, Set.of(SettingsOption.COLUMNNAMEORDER, SettingsOption.INDEXCOLUMNNAMEORDER), null, null, null, null, "string"));
    }

    @BeforeEach
    void beforeEach(){
    }

    boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    String getRegEx(String sampleSolution) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sampleSolution);

        if (statement instanceof CreateTable){
            deParser.deParse(((CreateTable) statement));
        }
        System.out.println(deParser.getBuffer().toString());
        return deParser.getBuffer().toString();
    }

    void validateListAgainstRegEx(String sampleSolution, List<String> alternativeStatements, boolean isAssertTrue) {
        this.buffer = new StringBuilder();
        deParser = new CreateTableDeParserForRegEx(buffer,settingsManager);


        try {
            String regex = this.getRegEx(sampleSolution);
            for (String str : alternativeStatements) {
                if (isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " /// " + regex);
                else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " /// " + regex);
            }
        }catch (JSQLParserException e){
            fail("Coulnd't parse the test Statement");
        }
    }

    @Test
    void createTableSimple() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (column1 datatype1)"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (column1 datatype1)", alternativeStatements, true);
    }

    @Test
    void createTemporaryAndIfNotExists(){
        List<String> alternativeStatements = List.of(
                "CREATE TEMPORARY TABLE IF NOT EXISTS table1 (column1 datatype1)"
        );

        validateListAgainstRegEx( "CREATE TEMPORARY TABLE IF NOT EXISTS table1 (column1 datatype1)", alternativeStatements, true);
    }

    @Test
    void createLIKE(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (column1 datatype1) LIKE old_tbl_name",
                "CREATE TABLE table1 (column1 datatype1) LIKE old_tbl_name"
        );

        validateListAgainstRegEx( "CREATE TABLE table1 (column1 datatype1) LIKE (old_tbl_name)", alternativeStatements, true);
    }

    @Test
    void withSingleColumnAndIndex() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1, INDEX index_name (col1))"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1, INDEX index_name (col1))", alternativeStatements, true);
    }

    @Test
    void withMultipleSimpleColumns() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (column1 datatype1, column2 datatype2, column3 datatype3)",
                "CREATE TABLE table1 (column2 datatype2, column1 datatype1, column3 datatype3)"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (column1 datatype1, column2 datatype2, column3 datatype3)", alternativeStatements, true);
    }

    @Test
    void withConstraint() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1))", alternativeStatements, true);
    }

    @Test
    void with2TableConstraint() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1, col2))",
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col2, col1))"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1, col2))", alternativeStatements, true);
    }

    @Test
    void withConstraintBetween2Columns() {
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1), col2 type2)"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1, CONSTRAINT my_constraint PRIMARY KEY (col1), col2 type2)", alternativeStatements, true);
    }

    /**
     * check if decision is to implement:
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1",
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT =1",
     *                 "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT= 1"
     */
    @Test
    void withOneTableOption(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1", alternativeStatements, true);
    }

    @Test
    void withOneTableOptionWithEquals(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT =1",
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT= 1"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1", alternativeStatements, true);

        List<String> statementsToFail = List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT1"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) AUTO_INCREMENT = 1", statementsToFail, false);
    }


    /**
     * , seperated Tableoptions isn't supported yet by deparser
     */
    @Test
    void withMultipleTableOptions(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1 COMMENT = 'comment'"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) AUTO_INCREMENT 1 COMMENT = 'comment'", alternativeStatements, true);
    }

    @Test
    void withOnePartitionOption(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)"
        );

        validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1)", alternativeStatements, true);
    }

    /**
     *  List<String> alternativeStatements = List.of(
     *                 "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT =
     *                 'String')"
     *         );
     *
     *         validateListAgainstRegEx("CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName
     *         VALUES IN (val1, val2))", alternativeStatements, true);
     */
    @Test
    void withOnePartitionOptionAndOnePartitionDefinition(){
        List<String> alternativeStatements = List.of(
                "CREATE TABLE table1 (col1 type1) PARTITION BY KEY (col1) (PARTITION partName COMMENT = 'String')"
        );
    }



    @AfterAll
    static void tearDown() throws Exception {
        Field field = UserSettings.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(UserSettings.getInstance(), null);
    }
}
