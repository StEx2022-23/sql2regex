package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class InsertDeParserForRegExTest{

    @Test
    void testInsertDeParserForRegExConstructor() {
        SettingsContainer settings = SettingsContainer.builder().build();
        InsertDeParserForRegEx insertDeParserForRegEx = new InsertDeParserForRegEx(settings);
        Assertions.assertNotNull(insertDeParserForRegEx);
    }

    @Test
    void oneValueList() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO table (col1, col2)); VALUES ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('2', '1')",
                "INSERT INTO table (col2, col1) VALUE ('2', '1')"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO table (col1, col2) VALUES ('1', '2')",
                matchingMap,
                true
        );
    }

    @Test
    void oneValueListFailing() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO table (col1, col2)); VALUES ('2', '1')"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO table (col1, col2) VALUES ('1', '2')",
                matchingMap,
                false
        );
    }

    @Test
    void twoValueList() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO table (col1, col2)); VALUES ('1', '2'), ('11', '22')",
                "INSERT INTO table (col1, col2) VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUE ('22', '11'), ('2', '1')",
                "INSERT INTO table (col2, col1) VALUES (22, 11), (2, 1)"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')",
                matchingMap,
                true
        );
    }

    @Test
    void twoValueListFailing() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO table (col2, col1)); VALUES ('11', '22'), ('1', '2')",
                "INSERT INTO table (col2, col1) VALUES ('22', '11'), ('1', '2')"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO table (col1, col2) VALUES ('1', '2'), ('11', '22')",
                matchingMap,
                false
        );
    }

    @Test
    void insertWithSet() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO table SET name = 'Kim', isBFF = true"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO table SET name = 'Kim', isBFF = true",
                matchingMap,
                true
        );
    }

    @Test
    void testReturning() {
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "INSERT INTO t1 VALUES (val1, val2)); RETURNING id"
        ));
        TestUtils.validateStatementAgainstRegEx(
                SettingsContainer.builder().build(),
                "INSERT INTO t1 VALUES (val1, val2) RETURNING id",
                matchingMap,
                true
        );
    }
}
