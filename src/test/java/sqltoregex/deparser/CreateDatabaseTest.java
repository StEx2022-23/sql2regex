package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CreateDatabaseTest {
    @Test
    void testShortConstructor(){
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        Assertions.assertDoesNotThrow(() -> new CreateDatabaseDeParserForRegEx(settingsContainer));
        CreateDatabaseDeParserForRegEx createDatabaseDeParserForRegEx = new CreateDatabaseDeParserForRegEx(settingsContainer);
        Assertions.assertNotNull(createDatabaseDeParserForRegEx);
    }

    @Test
    void testExtendedConstructor(){
        SettingsContainer settingsContainer = SettingsContainer.builder().build();
        StringBuilder stringBuilder = new StringBuilder();
        Assertions.assertDoesNotThrow(() -> new CreateDatabaseDeParserForRegEx(stringBuilder, settingsContainer));
        CreateDatabaseDeParserForRegEx createDatabaseDeParserForRegEx = new CreateDatabaseDeParserForRegEx(stringBuilder, settingsContainer);
        Assertions.assertNotNull(createDatabaseDeParserForRegEx);
    }
    @Test
    void deParseTest(){
        CreateDatabaseDeParserForRegEx createTableDeParserForRegEx = new CreateDatabaseDeParserForRegEx(SettingsContainer.builder().build());
        Assertions.assertNotEquals(0, createTableDeParserForRegEx.deParse("CREATE DATABASE table").length());
    }

    @Test
    void testCreateDatabaseStatement(){
        final String sampleSolution = "CREATE DATABASE table";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE DATABASE table",
                "CREATE    DATABASE    table",
                "CREATE DATABASE OR REPLACE table",
                "CREATE DATABASE IF NOT EXISTS table"
        ));
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "CRETE DATABASE table",
                "CREATE DATABSE table",
                "CREATE DATABSE tale"
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
    void testExtendedCreateDatabaseInput(){
        final String sampleSolution = "CREATE DATABASE database CHARACTER SET = 'utf8' COLLATE = 'utf8'";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "CREATE DATABASE database CHARACTER SET = 'utf8' COLLATE = 'utf8'",
                "CREATE   DATABASE   database   CHARACTER   SET   =   'utf8'   COLLATE   =    'utf8'"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }
}
