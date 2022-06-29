package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of {@link SynonymGenerator}. Saves strings and searches with exact representation of them.
 * e.g. used for: Data-Type synonyms
 */
public class StringSynonymGenerator extends SynonymGenerator<String, String> {

    /**
     * SynonymGenerator constructor. Needs one of enum {@link SettingsOption}.
     * Inits the super class {@link SynonymGenerator}.
     * @param settingsOption one of enum {@link SettingsOption}
     * @see SettingsOption
     * @see SynonymGenerator
     */
    public StringSynonymGenerator(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * Ensures that the string is not null. Returns the input string.
     * @param syn string synonym
     * @return input string
     */
    @Override
    protected String prepareSynonymForAdd(String syn) {
        Assert.notNull(syn, "Added string must not be null");
        return syn.toUpperCase();
    }

    /**
     * Ensures that the string is not null. Returns the input string.
     * @param wordToFindSynonyms string synonym
     * @return input string
     */
    @Override
    protected String prepareSynonymForSearch(String wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, "Query string must be not null");
        return wordToFindSynonyms.toUpperCase();
    }

    /**
     * Ensures that the strings are not null. Returns the input strings.
     * @param syn base synonym
     * @param wordToFindSynonyms word to find synonyms for
     * @return input string
     */
    @Override
    protected String prepareVertexForRegEx(String syn, String wordToFindSynonyms) {
        Assert.notNull(syn, "Vertex string must be not null");
        Assert.notNull(wordToFindSynonyms, "Query string must be not null");
        return syn.toUpperCase();
    }

    /**
     * Generates a regex with all possible synonyms, if the param synonymGenerator isn't null.
     * Otherwise, only the input string returned.
     * @param synonymGenerator {@link StringSynonymGenerator} object
     * @param str input string
     * @return generated regex or str
     */
    public static String useOrDefault(StringSynonymGenerator synonymGenerator, String str){
        if (null != synonymGenerator) return synonymGenerator.generateRegExFor(str.toUpperCase());
        else return str;
    }

    /**
     * Generates a list of strings with all possible synonyms, if the param synonymGenerator isn't null.
     * @param synonymGenerator {@link StringSynonymGenerator}
     * @param str input string
     * @return generated list of synonyms as string or given string as one entry in the string list
     */
    public static List<String> generateAsListOrDefault(StringSynonymGenerator synonymGenerator, String str){
        if (null != synonymGenerator) return synonymGenerator.generateAsList(str.toUpperCase());
        return new LinkedList<>(List.of(str));
    }
}
