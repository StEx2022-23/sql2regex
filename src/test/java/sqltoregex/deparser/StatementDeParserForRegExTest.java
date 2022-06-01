package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class StatementDeParserForRegExTest{

    @Test
    void testConstructorOne() {
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, new SettingsContainer());
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorThree() {
        SettingsContainer settings = new SettingsContainer();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            buffer, settings);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() {
        SettingsContainer settings = new SettingsContainer();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        StringBuilder buffer = new StringBuilder();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settings);
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            selectDeParserForRegEx,
                                                                                            buffer, settings);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void withClause() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "WITH temporaryTable(averageValue)); as (SELECT AVG(col2) from table2) SELECT col1 FROM table1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(), 
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1",
                matchingMap,
                false
        );
    }
}
