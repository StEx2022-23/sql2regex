package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsOption;

import java.util.*;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into account different spelling variants
 * of a word. It is assumed that omitting a character is "okay".
 * Example:
 * test ↔ (?:test|est|tst|tet|tes) or (test|est|tst|tet|tes)
 */
public class SpellingMistake implements RegExGenerator<String> {
    private final SettingsOption settingsOption;
    protected boolean isCapturingGroup = false;

    public SpellingMistake(SettingsOption settingsOption){
        this.settingsOption = settingsOption;
    }

    /**
     * Iterates through each letter of the word, storing all spelling variations assuming a letter is forgotten
     * @param str String
     * @return creates a regex non-capturing group with all calculated options
     */
    public String generateRegExFor(String str){
        if(str.isEmpty()){
            throw new IllegalArgumentException("String str should not be empty!");
        }
        StringBuilder alternativeWritingStyles = new StringBuilder();
        alternativeWritingStyles.append(isCapturingGroup ? '(' : "(?:");
        alternativeWritingStyles.append(str.replace("\\(", "\\\\(").replace("\\)", "\\\\)")).append('|');
        for(int i = 0; i<str.length(); i++){
            String first = str.substring(0, i);
            String second = str.substring(i+1);
            String concat = first.concat(second);
            concat = concat.replace("\\(", "\\\\(").replace("\\)", "\\\\)");
            alternativeWritingStyles.append(concat);
            alternativeWritingStyles.append("|");
        }
        alternativeWritingStyles.replace(alternativeWritingStyles.length()-1, alternativeWritingStyles.length(), "");
        alternativeWritingStyles.append(')');
        return alternativeWritingStyles.toString();
    }
    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    @Override
    public void setCapturingGroup(boolean capturingGroup) {
        this.isCapturingGroup = capturingGroup;
    }

    @Override
    public SettingsOption getSettingsOption() {
        return settingsOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpellingMistake that)) return false;
        return settingsOption == that.settingsOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingsOption);
    }
}
