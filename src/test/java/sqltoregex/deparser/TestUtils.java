package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Assertions;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils{

    private static boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    private static String getRegEx(SettingsContainer settings, String sampleSolution, boolean isExpression) {
        StringBuilder buffer = new StringBuilder();
        try {
            if (isExpression){
                Expression expression = CCJSqlParserUtil.parseExpression(sampleSolution);
                ExpressionDeParserForRegEx deParser = new ExpressionDeParserForRegEx(settings);
                deParser.setBuffer(buffer);
                expression.accept(deParser);
            }else {
                StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, settings);

                Statement statement = CCJSqlParserUtil.parse(sampleSolution);
                statement.accept(statementDeParserForRegEx);
            }
            return buffer.toString();
        }catch(JSQLParserException e){
            e.printStackTrace();
            fail("Exception while parsing");
        }
        //will never be reached
        return "";
    }

    public static String validateStatementAgainstRegEx(SettingsContainer settings, String sampleSolution, Map<SettingsOption, List<String>> matchingMap, boolean isAssertTrue) {
        String regex = getRegEx(settings, sampleSolution, false);
        List<String> alternativeStatements = new LinkedList<>();
        for (Map.Entry<SettingsOption, List<String>> entry : matchingMap.entrySet()){
            if (settings.get(entry.getKey()) != null){
                alternativeStatements.addAll(entry.getValue());
            }
        }

        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " <==> " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " <==> " + regex);
        }
        return regex;
    }

    public static String validateExpressionAgainstRegEx(SettingsContainer settings, String sampleSolution, Map<SettingsOption, List<String>> matchingMap, boolean isAssertTrue) {
        String regex = getRegEx(settings, sampleSolution, true);
        List<String> alternativeStatements = new LinkedList<>();
        for (Map.Entry<SettingsOption, List<String>> entry : matchingMap.entrySet()){
            if (settings.get(entry.getKey()) != null){
                alternativeStatements.addAll(entry.getValue());
            }
        }

        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " <==> " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " <==> " + regex);
        }
        return regex;
    }

    public static SettingsContainer getSettingsContainerWithAllSpellingMistakesAndOrderRotations(){
        SettingsContainer settingsContainer = new SettingsContainer();
        settingsContainer.putAll(getSettingsContainerWithAllSpellingMistakes());
        settingsContainer.putAll(getSettingsContainerWithAllOrderRotations());
        return settingsContainer;
    }

    public static SettingsContainer getSettingsContainerWithAllSpellingMistakes(){
        SettingsContainer settingsContainer = new SettingsContainer();
        SpellingMistake tableNameSpelling = new SpellingMistake(SettingsOption.TABLENAMESPELLING);
        SpellingMistake columnNameSpelling = new SpellingMistake(SettingsOption.COLUMNNAMESPELLING);
        SpellingMistake keywordSpelling = new SpellingMistake(SettingsOption.KEYWORDSPELLING);
        SpellingMistake indexColumnNameSpelling = new SpellingMistake(SettingsOption.INDEXCOLUMNNAMESPELLING);
        settingsContainer.with(tableNameSpelling).with(columnNameSpelling).with(keywordSpelling).with(indexColumnNameSpelling);
        return settingsContainer;
    }

    public static SettingsContainer getSettingsContainerWithAllOrderRotations(){
        SettingsContainer settingsContainer = new SettingsContainer();
        SpellingMistake tableOrder = new SpellingMistake(SettingsOption.TABLENAMEORDER);
        SpellingMistake columnOrder = new SpellingMistake(SettingsOption.COLUMNNAMEORDER);
        SpellingMistake groupByOrder = new SpellingMistake(SettingsOption.GROUPBYELEMENTORDER);
        settingsContainer.with(tableOrder).with(columnOrder).with(groupByOrder);
        return settingsContainer;
    }

}
