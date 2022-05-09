package sqltoregex.property;

import java.util.Collections;
import java.util.List;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into account different spelling variants
 * of a word. It is assumed that omitting a character is "okay".
 * Example:
 * test ↔ (?:test|est|tst|tet|tes)
 */
public class SpellingMistake implements Property{
    @Override
    public List<String> getSettings() {
        return Collections.singletonList("SpellingMistake");
    }

    /**
     * Iterates through each letter of the word, storing all spelling variations assuming a letter is forgotten
     * @param tablename String
     * @return creates a regex non-capturing group with all calculated options
     */
    public String calculateAlternativeWritingStyles(String tablename){
        if(tablename.isEmpty()){
            throw new IllegalArgumentException("Tablename should not be empty.");
        }
        StringBuilder alternativeWritingStyles = new StringBuilder();
        alternativeWritingStyles.append("(?:").append(tablename).append("|");
        for(int i = 0; i<tablename.length(); i++){
            alternativeWritingStyles.append(tablename, 0, i).append(tablename, i+1, tablename.length()).append("|");
        }
        alternativeWritingStyles.replace(alternativeWritingStyles.length()-1, alternativeWritingStyles.length(), "");
        alternativeWritingStyles.append(")");
        return alternativeWritingStyles.toString();
    }
}
