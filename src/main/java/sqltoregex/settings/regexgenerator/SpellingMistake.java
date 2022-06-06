package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

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

    @Override
    public List<String> generateAsList(String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String str should not be empty!");
        }
        List<String> stringList = new LinkedList<>();
        stringList.add(str);
        for(int i = 0; i<str.length(); i++){
            String first = str.substring(0, i);
            String second = str.substring(i + 1);
            String concat = first.concat(second);
            stringList.add(concat);
        }
        return stringList;
    }

    public static String useOrDefault(SpellingMistake spellingMistake, String str){
        if (null != spellingMistake) return spellingMistake.generateRegExFor(str);
        else return str;
    }

    public static List<String> generateAsListOrDefault(SpellingMistake spellingMistake, String str){
        if (null != spellingMistake) return spellingMistake.generateAsList(str);
        return new LinkedList<>(List.of(str));
    }
}
