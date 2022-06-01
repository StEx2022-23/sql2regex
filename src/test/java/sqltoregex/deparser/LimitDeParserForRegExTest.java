package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class LimitDeParserForRegExTest{
    
    @Test
    void testLimitDeparser(){
        StringBuilder buffer = new StringBuilder();
        LimitDeParserForRegEx limitDeParserForRegEx = new LimitDeParserForRegEx(buffer);
        Assertions.assertNotNull(limitDeParserForRegEx);
    }

    @Test
    void testLimit() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMT 3 OFFSET 2",
                "SELECT col1 LIMIT 3 OFFST 2",
                "SELECT col1 LIMIT    3   OFFSET    2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 LIMIT 2, 3",
                matchingMap,
                true
        );
    }

    @Test
    void testLimitNull() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMIT null"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 LIMIT null",
                matchingMap,
                true
        );
    }

    @Test
    void testLimitAndOffset() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMIT 3 OFFSET 2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 LIMIT 3 OFFSET 2",
                matchingMap,
                true
        );
    }

    @Test
    void testOffset() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 OFFSET 10 ROWS",
                "SELECT col1 OFFSET  10  ROWS",
                "SELECT col1 OFFST 10 RWS"
        ));
        TestUtils.validateStatementAgainstRegEx(
                new SettingsContainer(),
                "SELECT col1 OFFSET 10 ROWS",
                matchingMap,
                true
        );
    }
}
