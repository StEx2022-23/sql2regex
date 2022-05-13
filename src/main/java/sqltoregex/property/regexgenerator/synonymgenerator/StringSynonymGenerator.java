package sqltoregex.property.regexgenerator.synonymgenerator;

import org.springframework.util.Assert;
import sqltoregex.property.PropertyOption;

/**
 * Default implementation of {@link SynonymGenerator}. Saves Strings and searches with exact representation of them.
 * e.g. used for: Data-Type synonyms
 */
public class StringSynonymGenerator extends SynonymGenerator<String, String> {

    public StringSynonymGenerator(PropertyOption propertyOption) {
        super(propertyOption);
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
}
