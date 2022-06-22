package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class DropDeParserForRegExTest {
    @Test
    void testShortConstructor(){
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        Assertions.assertDoesNotThrow(() -> new DropDeParserForRegEx(settingsContainer));
        DropDeParserForRegEx dropDeParserForRegEx = new DropDeParserForRegEx(settingsContainer);
        Assertions.assertNotNull(dropDeParserForRegEx);
    }

    @Test
    void testExtendedConstructor(){
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        StringBuilder stringBuilder = new StringBuilder();
        Assertions.assertDoesNotThrow(() -> new DropDeParserForRegEx(stringBuilder, settingsContainer));
        DropDeParserForRegEx dropDeParserForRegEx = new DropDeParserForRegEx(stringBuilder, settingsContainer);
        Assertions.assertNotNull(dropDeParserForRegEx);
    }

    @Test
    void testDropStatement(){
        final String sampleSolution = "DROP TABLE tab1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DROP TABLE tab1",
                "DROP   TABLE   tab1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "DROP TBLE tab1",
                "DOP TABLE tab1"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "DROP TABLE ta1",
                "DROP TABLE tab1"
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
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testDropWithIfExistsStatement(){
        final String sampleSolution = "DROP TABLE IF EXISTS tab1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DROP TABLE IF EXISTS tab1",
                "DROP   TABLE  IF   EXISTS tab1"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "DROP TBLE IF EXISTS tab1",
                "DOP TABLE IF EXITS tab1"
        ));
        matchingMap.put(SettingsOption.TABLENAMESPELLING, List.of(
                "DROP TABLE IF EXISTS ta1",
                "DROP TABLE IF EXISTS tab1"
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
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void testDropStatementWithQuotationMarks(){
        final String sampleSolution = "DROP TABLE `tab1`";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "DROP TABLE tab1",
                "DROP   TABLE   \"tab1\""
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }
}
