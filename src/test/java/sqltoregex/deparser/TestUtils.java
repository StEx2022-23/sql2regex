package sqltoregex.deparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sqltoregex.settings.SettingsManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TestUtils {
    StatementDeParserForRegEx statementDeParserForRegEx;

    @Autowired
    public TestUtils(SettingsManager settingsManager) {
        StringBuilder buffer = new StringBuilder();
        this.statementDeParserForRegEx = new StatementDeParserForRegEx(buffer, settingsManager);
    }

    private boolean checkAgainstRegEx(String regex, String toBeChecked) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(toBeChecked);
        return matcher.matches();
    }

    private String getRegEx(String sampleSolution) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sampleSolution);
        statement.accept(this.statementDeParserForRegEx);
        System.out.println(this.statementDeParserForRegEx.getBuffer().toString());
        return this.statementDeParserForRegEx.getBuffer().toString();
    }

    public void validateListAgainstRegEx(String sampleSolution, List<String> alternativeStatements, boolean isAssertTrue) throws JSQLParserException {
        String regex = this.getRegEx(sampleSolution);
        for(String str : alternativeStatements){
            if(isAssertTrue) Assertions.assertTrue(checkAgainstRegEx(regex, str), str + " " + regex);
            else Assertions.assertFalse(checkAgainstRegEx(regex, str), str + " " + regex);
        }
    }

}
