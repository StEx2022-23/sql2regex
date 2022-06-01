package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Assertions;
import sqltoregex.settings.SettingsContainer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils{

    private static boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    private static String getRegEx(SettingsContainer settings, String sampleSolution) throws JSQLParserException {
        StatementDeParserForRegEx statementDeParserForRegEx = new StatementDeParserForRegEx(new StringBuilder(),
                                                                                            settings);

        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(statementDeParserForRegEx);
        return statementDeParserForRegEx.getBuffer().toString();
    }

    public static void validateListAgainstRegEx(SettingsContainer settings, String sampleSolution, List<String> alternativeStatements, boolean isAssertTrue) throws JSQLParserException {
        String regex = getRegEx(settings, sampleSolution);
        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " /// " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " /// " + regex);
        }
    }

}
