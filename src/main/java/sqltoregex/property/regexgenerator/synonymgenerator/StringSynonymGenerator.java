package sqltoregex.property.regexgenerator.synonymgenerator;

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
        return syn;
    }

    @Override
    protected String prepareSynonymForSearch(String wordToFindSynonyms) {
        return wordToFindSynonyms;
    }

    @Override
    protected String prepareVertexForRegEx(String syn, String wordToFindSynonyms) {
        return syn;
    }
}
