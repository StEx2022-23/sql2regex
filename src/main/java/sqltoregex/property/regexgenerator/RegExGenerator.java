package sqltoregex.property.regexgenerator;

public interface RegExGenerator<T> {
    String generateRegExFor(T wordToFindSynonyms);
    void setCapturingGroup(boolean capturingGroup);
}
