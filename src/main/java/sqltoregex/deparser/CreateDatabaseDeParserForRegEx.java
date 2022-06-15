package sqltoregex.deparser;

import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements an own create database statement deparser to generate regular expressions.
 */
public class CreateDatabaseDeParserForRegEx {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private final StringBuilder buffer;
    SpellingMistake keywordSpellingMistake;

    /**
     * Short constructor for CreateDatabaseDeParserForRegEx.
     * @param settings {@link SettingsContainer}
     */
    public CreateDatabaseDeParserForRegEx(SettingsContainer settings){
        this(new StringBuilder(), settings);
    }

    /**
     * Extended constructor for CreateDatabaseDeParserForRegEx.
     * @param buffer {@link StringBuilder}
     * @param settings {@link SettingsContainer}
     */
    public CreateDatabaseDeParserForRegEx(StringBuilder buffer, SettingsContainer settings){
        this.buffer = buffer;
        this.keywordSpellingMistake =  settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
    }

    /**
     * Implements own create database deparser method. Reduce complexity of the statement by only select databasename, without any other parameters.
     * The matching regex is: CREATE DATABASE (.*)( .*)*
     * @param createDatabase {@link String}
     * @return generated regex
     */
    public String deParse(String createDatabase){
        Pattern pattern = Pattern.compile("CREATE DATABASE (\\w*)( .*)*");
        Matcher matcher = pattern.matcher(createDatabase);
        StringBuilder databasename = new StringBuilder();
        StringBuilder statementAfterDatabaseName = new StringBuilder();
        if(matcher.matches()){
            databasename.append(matcher.group(1));
            statementAfterDatabaseName.append(matcher.group(2));
        }

        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "CREATE"));
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "DATABASE"));
        buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append("(");
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "OR"))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "REPLACE"))
                    .append(REQUIRED_WHITE_SPACE)
                    .append("|");
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "IF"))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "NOT"))
                    .append(REQUIRED_WHITE_SPACE)
                    .append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "EXISTS"))
                    .append(REQUIRED_WHITE_SPACE);
            buffer.append(")?");
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, databasename.toString()));
        if(!statementAfterDatabaseName.toString().equals("null")){
            buffer.append(statementAfterDatabaseName.toString().replace(" ", REQUIRED_WHITE_SPACE));
        }
        return buffer.toString();
    }
}
