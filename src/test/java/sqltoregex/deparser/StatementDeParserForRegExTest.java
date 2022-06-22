package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.*;

class StatementDeParserForRegExTest {

    @Test
    void testConstructorOne() {
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer,
                                                                                            SettingsContainer.builder()
                                                                                                    .build());
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorThree() {
        SettingsContainer settings = SettingsContainer.builder().build();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        StringBuilder buffer = new StringBuilder();
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(expressionDeParserForRegEx,
                                                                                            buffer, settings);
        Assertions.assertNotNull(statementDeParserForRegEx);
    }

    @Test
    void testConstructorTwo() {
        SettingsContainer settings = SettingsContainer.builder().build();
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
                SettingsContainer.builder().build(),
                "WITH temporaryTable(averageValue) as (SELECT AVG(col2) from table2) SELECT col1 FROM table1",
                matchingMap,
                false
        );
    }

    @Test
    void testaddQuotationMarks() {
        Map<SettingsOption, List<String>> matchingMapNormal = new EnumMap<>(SettingsOption.class);
        matchingMapNormal.put(SettingsOption.DEFAULT, List.of(
            "string",
            "'string'",
            "´string´",
            "`string`",
            "\"string\""
            ));
        for (String val : matchingMapNormal.get(SettingsOption.DEFAULT)){
            TestUtils.checkAgainstRegEx(StatementDeParserForRegEx.addQuotationMarks("string"), val);
        }

        Map<SettingsOption, List<String>> matchingMapWhitespace = new EnumMap<>(SettingsOption.class);
        matchingMapWhitespace.put(SettingsOption.DEFAULT, List.of(
            "'str ing'",
            "´str ing´",
            "`str ing`",
            "\"str ing\""
            ));
        for (String val : matchingMapWhitespace.get(SettingsOption.DEFAULT)){
            TestUtils.checkAgainstRegEx(StatementDeParserForRegEx.addQuotationMarks("str ing"), val);
        }

        Map<SettingsOption, List<String>> notMatchingMapWhitespace = new EnumMap<>(SettingsOption.class);
        notMatchingMapWhitespace.put(SettingsOption.DEFAULT, List.of(
            "str ing"
            ));
        for (String val : notMatchingMapWhitespace.get(SettingsOption.DEFAULT)){
            TestUtils.checkAgainstRegEx(StatementDeParserForRegEx.addQuotationMarks("str ing"), val);
        }

      }
}
