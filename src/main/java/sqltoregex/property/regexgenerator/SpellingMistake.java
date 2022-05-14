package sqltoregex.property.regexgenerator;

import sqltoregex.property.Property;
import sqltoregex.property.PropertyOption;

import java.util.*;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into account different spelling variants
 * of a word. It is assumed that omitting a character is "okay".
 * Example:
 * test ↔ (?:test|est|tst|tet|tes)
 */
public class SpellingMistake implements Property<PropertyOption>, RegExGenerator<String> {
    private final PropertyOption propertyOption;
    protected boolean isCapturingGroup = false;

    public SpellingMistake(PropertyOption propertyOption){
        this.propertyOption = propertyOption;
    }

    @Override
    public Set<PropertyOption> getSettings() {
        return new HashSet<>(List.of(propertyOption));
    }

    /**
     * Iterates through each letter of the word, storing all spelling variations assuming a letter is forgotten
     * @param tableName String
     * @return creates a regex non-capturing group with all calculated options
     */
    public String generateRegExFor(String tableName){
        if(tableName.isEmpty()){
            throw new IllegalArgumentException("Tablename should not be empty.");
        }
        StringBuilder alternativeWritingStyles = new StringBuilder();
        alternativeWritingStyles.append(isCapturingGroup ? '(' : "(?:");
        alternativeWritingStyles.append(tableName).append('|');
        for(int i = 0; i<tableName.length(); i++){
            alternativeWritingStyles.append(tableName, 0, i).append(tableName, i+1, tableName.length()).append("|");
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpellingMistake)) return false;
        SpellingMistake that = (SpellingMistake) o;
        return propertyOption == that.propertyOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyOption);
    }
}
