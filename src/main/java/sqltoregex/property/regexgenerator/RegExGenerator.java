package sqltoregex.property.regexgenerator;

public interface RegExGenerator<T> {
    public String generateRegExFor(T wordToFindSynonyms);
    public void setCapturingGroup(boolean capturingGroup);
}
