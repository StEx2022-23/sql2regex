package sqltoregex.property.regexgenerator;

public interface RegExGenerator<T> {
    public String generateRegExFor(T wordToFindSynonyms);
}
