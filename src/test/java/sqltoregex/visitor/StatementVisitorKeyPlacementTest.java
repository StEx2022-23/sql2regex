package sqltoregex.visitor;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.deparser.StatementDeParserForRegEx;
import sqltoregex.deparser.TestUtils;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

class StatementVisitorKeyPlacementTest {

    @Test
    void keyPlacementLikeUser() {
        final String sampleSolution = "CREATE TABLE table1 (col1 type1, col2 type2 UNIQUE, CONSTRAINT my_constraint PRIMARY KEY (col1))";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE TABLE table1 (col1 type1, col2 type2 UNIQUE, CONSTRAINT my_constraint PRIMARY KEY (col1))"
        ));

        Assertions.assertTrue(TestUtils.checkAgainstRegEx(
                parseWithKeyPlacementOption(sampleSolution, StatementVisitorKeyPlacement.KeyPlacementOption.USER_INPUT),
                sampleSolution)
        );
    }

    @Test
    void keyPlacementAllInColumn() {
        final String sqlInput = "CREATE TABLE table1 (col1 type1, col2 type2, CONSTRAINT my_constraint PRIMARY KEY (col1), CONSTRAINT my_constraint UNIQUE KEY (col2))";
        String regEx = parseWithKeyPlacementOption(sqlInput, StatementVisitorKeyPlacement.KeyPlacementOption.ONLY_COLUMN);

        Assertions.assertTrue(TestUtils.checkAgainstRegEx(
                regEx,
                "CREATE TABLE table1 (col1 type1 PRIMARY KEY, col2 type2 UNIQUE KEY)"), "failed with regEx: " + regEx
        );
    }

    @Test
    void keyPlacementAllInConstraints() {
        final String sqlInput = "CREATE TABLE table1 (col1 type1 PRIMARY KEY, col2 type2 UNIQUE KEY)";
        String regEx = parseWithKeyPlacementOption(sqlInput, StatementVisitorKeyPlacement.KeyPlacementOption.ONLY_CONSTRAINT);

        Assertions.assertTrue(TestUtils.checkAgainstRegEx(
                regEx,
                "CREATE TABLE table1 (col1 type1, col2 type2, CONSTRAINT my_constraint PRIMARY KEY (col1), CONSTRAINT UNIQUE KEY (col2))"), "failed with regEx: " + regEx
        );
    }


    private String parseWithKeyPlacementOption(String sqlString, StatementVisitorKeyPlacement.KeyPlacementOption placementOption){
        try {
            StatementVisitor visitor = new StatementVisitorKeyPlacement(placementOption);
            Statement statement = CCJSqlParserUtil.parse(sqlString);
            statement.accept(visitor);

            SettingsContainer settings = SettingsContainer.builder().build();
            StringBuilder buffer = new StringBuilder();
            StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, settings);

            statement.accept(statementDeParserForRegEx);
            return buffer.toString();
        }catch(JSQLParserException e){
            fail("Could not parse statement " + e);
        }
        return "";
    }
}
