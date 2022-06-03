package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.SettingsOption;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into account different spelling variants
 * of a word. It is assumed that omitting a character is "okay".
 * Example:
 * test ↔ (?:test|est|tst|tet|tes) or (test|est|tst|tet|tes)
 */
public class SpellingMistake extends RegExGenerator<String> {
    public SpellingMistake(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * Iterates through each letter of the word, storing all spelling variations assuming a letter is forgotten
     *
     * @param str String
     * @return creates a regex non-capturing group with all calculated options
     */
    public String generateRegExFor(String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String str should not be empty!");
        }
        StringBuilder alternativeWritingStyles = new StringBuilder();
        alternativeWritingStyles.append(isNonCapturingGroup ? "(?:" : "(");
        alternativeWritingStyles.append(str);
        if(str.length() > 1) alternativeWritingStyles.append("|");
        for(int i = 0; i<str.length(); i++){
            String first = str.substring(0, i);
            String second = str.substring(i + 1);
            String concat = first.concat(second);
            alternativeWritingStyles.append(concat);
            alternativeWritingStyles.append("|");
        }
        alternativeWritingStyles.replace(alternativeWritingStyles.length() - 1, alternativeWritingStyles.length(), "");
        alternativeWritingStyles.append(')');
        return alternativeWritingStyles.toString();
    }
}
