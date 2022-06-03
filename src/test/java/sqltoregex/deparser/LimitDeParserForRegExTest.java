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
        SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMT 3 OFFSET 2",
                "SELECT col1 LIMIT 3 OFFST 2",
                "SELECT col1 LIMIT    3   OFFSET    2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                "SELECT col1 LIMIT 2, 3",
                matchingMap,
                true
        );
    }

    @Test
    void testLimitNull() {
        final String sampleSolution = "SELECT col1 LIMIT null";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMIT null",
                "SELECT col1   LIMIT   null"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT col1 LIMT null"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 LIMIT null",
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
    void testLimitAndOffset() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 LIMIT 3 OFFSET 2",
                "SELECT col1   LIMIT   3    OFFSET    2"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT col1 LIIT 3 OFSET 2"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "SELECT col1 LIMIT 3 OFFSET 2",
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                "SELECT col1 LIMIT 3 OFFSET 2",
                matchingMap,
                true
        );
    }

    @Test
    void testOffset() {
        final String sampleSolution = "SELECT col1 OFFSET 10 ROWS";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "SELECT col1 OFFSET 10 ROWS",
                "SELECT col1 OFFSET  10  ROWS"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "SELECT col1 OFFST 10 RWS"
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
}
