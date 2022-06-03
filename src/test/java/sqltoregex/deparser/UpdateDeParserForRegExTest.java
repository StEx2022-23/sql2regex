package sqltoregex.deparser;

import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class UpdateDeParserForRegExTest{

    @Test
    void simpleUpdateStatement() {
        String sampleSolution = "UPDATE table1 SET col1 = 1, col1 = 11, col2 = 2, col2 = 22 ORDER BY col1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 SET col1 = 1, col1 = 11, col2 = 2, col2 = 22 ORDER BY col1",
                "UPDATE table1 SET col1 = 11, col1 = 1, col2 = 2, col2 = 22 ORDER BY col1",
                "UPDATE table1 SET col2 = 2, col2 = 22, col1 = 1, col1 = 11 ORDER BY col1"
        ));

        Map<SettingsOption, List<String>> matchingMapWithoutSettings = new EnumMap<>(SettingsOption.class);
        matchingMapWithoutSettings.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 SET col1 = 1, col1 = 11, col2 = 2, col2 = 22 ORDER BY col1",
                "UPDATE   table1   SET   col1   = 1,  col1  =11,col2 =  2, col2 = 22 ORDER BY col1"
        ));

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMapWithoutSettings,
                true
        );
    }

    @Test
    void complexUpdateWithAlias() {
        String sampleSolution = "UPDATE table1 t1, table2 t2 SET t1.col1 = 1, t2.col1 = 11, t1.col2 = 2, t2.col2 = 22 ORDER BY col1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table t2 SET t1.col1 = 1, t2.col1 = 11, t1.col2 = 2, t2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET table1.col1 = 1, t2.col1 = 11, t1.col2 = 2, table2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET table1.col1 = 1, t1.col2 = 2, t2.col1 = 11, table2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET tabe1.col1 = 1, 1.col2 = 2, t2.ol1 = 11, tabe2.col2 = 22 ORDER BY col1"
        ));

        Map<SettingsOption, List<String>> matchingMapWithoutSettings = new EnumMap<>(SettingsOption.class);
        matchingMapWithoutSettings.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table2 t2 SET t1.col1 = 1, t2.col1 = 11, t1.col2 = 2, t2.col2 = 22 ORDER BY col1",
                "UPDATE    table1 t1,   table2 t2 SET t1.col1=1, t2.col1 =   11, t1.col2 = 2, t2.col2   = 22 ORDER BY col1"

        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMESPELLING).with(SettingsOption.COLUMNNAMESPELLING).with(SettingsOption.TABLENAMEORDER).with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );

        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMapWithoutSettings,
                true
        );
    }

    @Test
    void outputCLause() {
        final String sampleSolution = "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.KEYWORDSPELLING, List.of(
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2",
                "UPDATE table1 SET col1 = 1, col2 = 2 OUPUT inserted.col1, inserted.col2"
        ));
        matchingMap.put(SettingsOption.COLUMNNAMEORDER, List.of(
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2",
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col2, inserted.col1",
                "UPDATE table1 SET col1 = 1, col2 = 2  OUTPUT  inserted.col2 , inserted.col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.KEYWORDSPELLING).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.COLUMNNAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void simpleJoin() {
        final String sampleSolution = "UPDATE table1, table2 SET table1.col1 = 1, table2.col1 = 1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 ,   table2 SET table1.col1 = 1  ,  table2.col1   = 1"
        ));
        matchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "UPDATE table1, table2 SET table1.col1 = 1, table2.col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void tableNameAlias() {
        final String sampleSolution = "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1";
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1",
                "UPDATE table1 t1, table2 t2 SET t1.col1 = 1, t2.col1 = 1",
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, table2.col1 = 1"
        ));
        matchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1",
                "UPDATE table2 t2, table1 t1 SET t1.col1 = 1, t2.col1 = 1",
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, table2.col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                sampleSolution,
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                sampleSolution,
                matchingMap,
                true
        );
    }

    @Test
    void tableNameAliasVariations() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table2 SET table1.col1 = 1, t2.col1 = 1",
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1"
        ));
        matchingMap.put(SettingsOption.TABLENAMEORDER, List.of(
                "UPDATE table2, table1 t1  SET table1.col1 = 1, t2.col1 = 1",
                "UPDATE table2 t2, table1 t1 SET table1.col1 = 1, t2.col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1 t1, table2 SET table1.col1 = 1, t2.col1 = 1",
                matchingMap,
                true
        );
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().with(SettingsOption.TABLENAMEORDER).build(),
                "UPDATE table1 t1, table2 SET table1.col1 = 1, t2.col1 = 1",
                matchingMap,
                true
        );
    }
}
