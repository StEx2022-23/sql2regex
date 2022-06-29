package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

/**
 * The SpellingMistake class allows creating a RegEx expression that takes into account different spelling variants
 * of a word. It is assumed that omitting a character is "okay".
 * Example:
 * test ↔ (?:test|est|tst|tet|tes) or (test|est|tst|tet|tes)
 */
public class SpellingMistake extends RegExGenerator<String> {
    /**
     * Constructor of SpellingMistake. Init the super class.
     * @param settingsOption one of enum {@link SettingsOption}
     * @see RegExGenerator
     */
    public SpellingMistake(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * Generates a list of strings which allowed spelling mistakes. We assume, that one wrong character in the string is "okay".
     * @param str current word
     * @return list of string with alternative spellings
     */
    @Override
    public List<String> generateAsList(String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String str should not be empty!");
        }
        List<String> stringList = new LinkedList<>();
        stringList.add(".?" + str);
        for(int i = 0; i<str.length(); i++){
            String first = str.substring(0, i);
            String second = str.substring(i + 1);
            String concat = first.concat(".?").concat(second);
            stringList.add(concat);
        }
        stringList.add(str + ".?");
        return stringList;
    }

    /**
     * Generates a regex with allowed spelling mistakes, if the param spellingMistake isn't null.
     * Otherwise, only the string is returned.
     * @param spellingMistake SpellingMistake object
     * @param str string to handle
     * @return generated regex or str
     */
    public static String useOrDefault(SpellingMistake spellingMistake, String str){
        if (null != spellingMistake) return spellingMistake.generateRegExFor(str);
        else return str;
    }

    /**
     * Generates a list of strings with allowed spelling mistakes, if the param spellingMistake isn't null.
     * Otherwise, a list of string with only one entry is returned. The entry is the given string.
     * @param spellingMistake SpellingMistake object
     * @param str string to handle
     * @return generated list of strings (with one or more entries)
     */
    public static List<String> generateAsListOrDefault(SpellingMistake spellingMistake, String str){
        if (null != spellingMistake) return spellingMistake.generateAsList(str);
        return new LinkedList<>(List.of(str));
    }
}
