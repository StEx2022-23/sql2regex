package sqltoregex.deparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class ExpressionDeParserForRegExTest {

    private void assertIsNonCapturingGroup(String regEx) {
        Assertions.assertTrue(regEx.contains("(?:"), "Expected nonCapturing Group");
        Assertions.assertTrue(regEx.contains(")"));
    }

    /**
     * Assertions.assertEquals("a\\s+BETWEEN\\s+1\\s+AND\\s+5", b.toString());
     */
    @Test
    void between() {
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "a BETWEEN 1 AND 5",
                " a BETWEEN 1 AND 5 ",
                "a    BETWEEN   1   AND    5"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "a BETWEEN 1 AND 5", matchingMap, true);
    }

    /**
     * Assertions.assertEquals("3\\s*\\s*2", b.toString());
     */
    @Test
    void binaryExpression() {
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "3 / 2",
                "3/2",
                "3  /  2"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "3 / 2", matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "3:2"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "3 / 2", notMatchingMap, false);
        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "4 DIV 2", notMatchingMap, false);
        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "4 >> 2", notMatchingMap, false);
        TestUtils.validateExpressionAgainstRegEx(settingsContainer, "4 << 2", notMatchingMap, false);
    }

    /**
     * Assertions.assertEquals("1\\s+AND\\s+2|2\\s+AND\\s+1", b.toString());
     */
    @Test
    void commutativeBinaryExpression() {
        final String sampleSolution = "1 AND 2";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "1 AND 2",
                "1   AND   2",
                "2 AND 1"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "1 UND 2",
                "1   &&   2"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void dateValue() {
        final String sampleSolution = "{d'2022-05-17'}";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2022-05-17",
                "2022-5-17"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "17.05.2022"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void exists() {
        final String sampleSolution = "NOT EXISTS b";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "NOT EXISTS b",
                "NOT  EXISTS  b"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void fullTextSearch() {
        final String sampleSolution = "MATCH (col1, col2) AGAINST ('text' IN NATURAL LANGUAGE MODE)";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "MATCH (col1,col2) AGAINST ('text' IN NATURAL LANGUAGE MODE)",
                "MATCH  (col1 , col2)  AGAINST  ( 'text' IN NATURAL LANGUAGE MODE)",
                "MATCH (col1,col2) AGAINST (\"text\" IN NATURAL LANGUAGE MODE)"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void inExpression() {
        final String sampleSolution = "2(+) NOT IN 5";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2(+) NOT IN 5",
                "2(+)  NOT IN  5"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void isBooleanTrue() {
        final String sampleSolution = "2 IS NOT TRUE";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 IS NOT TRUE",
                "2  IS  NOT  TRUE"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 ISNOT TRUE",
                "2 IS NOTTRUE"
        ));

       TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void isBooleanFalse() {
        final String sampleSolution = "2 IS NOT FALSE";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 IS NOT FALSE",
                "2  IS  NOT  FALSE"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 ISNOT FALSE",
                "2 IS NOTFALSE"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void isNullExpression() {
        final String sampleSolution = "2 ISNULL";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 ISNULL",
                "2 IS NULL"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void isNotNullExpression() {
        final String sampleSolution = "2 IS NOT NULL";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 NOT ISNULL",
                "2 NOT IS NULL",
                "2 IS NOT NULL"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2 IS NOTNULL"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void like() {
        final String sampleSolution = "a NOT LIKE b";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "a NOT LIKE b"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true);

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "a NOTLIKE b"
        ));

        TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false);
    }

    @Test
    void minorThan() {
        final String sampleSolution = "5<8";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5<8",
                "5< 8",
                "5< 8",
                "5 < 8",
                "8 > 5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5 > 8"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void minorThanEquals() {
        final String sampleSolution = "5<=8";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5<=8",
                "5<= 8",
                "5<= 8",
                "5 <= 8",
                "8 >= 5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5 >= 8"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void multiplication() {
        final String sampleSolution = "5*8";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5* 8",
                "5 *8",
                "8*5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(

        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void not() {
        final String sampleSolution = "NOT 5";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "NOT 5",
                "NOT  5",
                "!5",
                "!  5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "NICHT 5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void notEqualsTo() {
        final String sampleSolution = "5!= NULL";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "5!=NULL",
                "NULL!=5"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(

        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void oldOracleJoinBinaryExpression() {
        final String sampleSolution = "2(+) = 5";
        SettingsContainer settingsContainer = new SettingsContainer();
        Map<SettingsOption, List<String>> matchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(
                "2(+) = 5",
                "2(+)=5",
                "5 = 2(+)",
                "5=2(+)"
        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, matchingMap, true)
        );

        Map<SettingsOption, List<String>> notMatchingMap = new EnumMap<>(SettingsOption.class);
        matchingMap.put(SettingsOption.DEFAULT, List.of(

        ));

        assertIsNonCapturingGroup(
                TestUtils.validateExpressionAgainstRegEx(settingsContainer, sampleSolution, notMatchingMap, false)
        );
    }

    @Test
    void testFullConstructor() {
        StringBuilder buffer = new StringBuilder();
        SettingsContainer settings = new SettingsContainer();
        SelectDeParserForRegEx selectDeParserForRegEx = new SelectDeParserForRegEx(settings);
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(settings);
        OrderByDeParserForRegEx orderByDeParserForRegEx = new OrderByDeParserForRegEx(expressionDeParserForRegEx,
                buffer, settings);
        ExpressionDeParserForRegEx expressionDeParserForRegExTwo = new ExpressionDeParserForRegEx(
                selectDeParserForRegEx, buffer, orderByDeParserForRegEx, settings);
        Assertions.assertNotNull(expressionDeParserForRegExTwo);
    }
}
