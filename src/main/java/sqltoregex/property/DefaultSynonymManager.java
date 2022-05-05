package sqltoregex.property;

/**
 * Default implementation of {@link SynonymManager}. Saves Strings and searches with exact representation of them.
 * e.g. used for: Data-Type synonyms
 */
class DefaultSynonymManager extends SynonymManager<String, String> {

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
