package sqltoregex.settings.regexgenerator.synonymgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of {@link SynonymGenerator}. Saves Strings and searches with exact representation of them.
 * e.g. used for: Data-Type synonyms
 */
public class StringSynonymGenerator extends SynonymGenerator<String, String> {

    public StringSynonymGenerator(SettingsOption settingsOption) {
        super(settingsOption);
    }

    @Override
    protected String prepareSynonymForAdd(String syn) {
        Assert.notNull(syn, "Added string must be not null");
        return syn;
    }

    @Override
    protected String prepareSynonymForSearch(String wordToFindSynonyms) {
        Assert.notNull(wordToFindSynonyms, "Query string must be not null");
        return wordToFindSynonyms;
    }

    @Override
    protected String prepareVertexForRegEx(String syn, String wordToFindSynonyms) {
        Assert.notNull(syn, "Vertex string must be not null");
        Assert.notNull(wordToFindSynonyms, "Query string must be not null");
        return syn;
    }

    public static String useOrDefault(StringSynonymGenerator synonymGenerator, String str){
        if (null != synonymGenerator) return synonymGenerator.generateRegExFor(str);
        else return str;
    }

    public static List<String> generateAsListOrDefault(StringSynonymGenerator synonymGenerator, String str){
        if (null != synonymGenerator) return synonymGenerator.generateAsList(str);
        return new LinkedList<>(List.of(str));
    }
}
