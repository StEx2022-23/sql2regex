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
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 SET col1 = 1, col1 = 11, col2 = 2, col2 = 22 ORDER BY col1",
                "UPDATE table1 SET col1 = 11, col1 = 1, col2 = 2, col2 = 22 ORDER BY col1",
                "UPDATE table1 SET col2 = 2, col2 = 22, col1 = 1, col1 = 11 ORDER BY col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(), 
                "UPDATE table1 SET col1 = 1, col1 = 11, col2 = 2, col2 = 22 ORDER BY col1",
                matchingMap,
                true
        );
    }

    @Test
    void complexUpdateWithAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table t2 SET t1.col1 = 1, t2.col1 = 11, t1.col2 = 2, t2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET table1.col1 = 1, t2.col1 = 11, t1.col2 = 2, table2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET table1.col1 = 1, t1.col2 = 2, t2.col1 = 11, table2.col2 = 22 ORDER BY col1",
                "UPDATE table1 t1, table t2 SET tabe1.col1 = 1, 1.col2 = 2, t2.ol1 = 11, tabe2.col2 = 22 ORDER BY col1"

        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1 t1, table2 t2 SET t1.col1 = 1, t2.col1 = 11, t1.col2 = 2, t2.col2 = 22 ORDER BY col1",
                matchingMap,
                true
        );
    }

    @Test
    void outputCLause() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2",
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col2, inserted.col1",
                "UPDATE table1 SET col1 = 1, col2 = 2  OUTUT  inserted.col2 , inserted.col1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1 SET col1 = 1, col2 = 2 OUTPUT inserted.col1, inserted.col2",
                matchingMap,
                true
        );
    }

    @Test
    void simpleJoin() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1, table2 SET table1.col1 = 1, table2.col1 = 1",
                "UPDATE table2, table1 SET table1.col1 = 1, table2.col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1, table2 SET table1.col1 = 1, table2.col1 = 1",
                matchingMap,
                true
        );
    }

    @Test
    void tableNameAlias() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1 t1, table2 t2 SET table1.col1 = 1, t2.col1 = 1",
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
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "UPDATE table1 t1, table2 SET table1.col1 = 1, t2.col1 = 1",
                matchingMap,
                true
        );
    }
}
