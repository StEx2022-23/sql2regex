package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RegExGenerator<T> implements IRegExGenerator<T> {
    protected static final String OPTIONAL_WHITE_SPACE = "\\s*";
    protected static final String ELEMENT_DELIMITER = "|";
    protected boolean isNonCapturingGroup = true;
    private final SettingsOption settingsOption;

    protected RegExGenerator(SettingsOption settingsOption){
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.settingsOption = settingsOption;
    }

    /**
     * Generates a regular expression part String with the pre-/ and suffixes set <b>including</b> the param.
     *
     * @param wordToFindSynonyms
     * @return
     */
    public String generateRegExFor(T wordToFindSynonyms) {
        return this.joinListToRegEx(this.generateAsList(wordToFindSynonyms));
    }

    public String joinListToRegEx(List<String> elList){
        return RegExGenerator.joinListToRegEx(this, elList);
    }

    /**
     * For joining Lists in a static context. Extracting {@link RegExGenerator#isNonCapturingGroup} from the Parameter for right parsing.
     * @param regexGenerator
     * @param elList
     * @return joined by {@link RegExGenerator#ELEMENT_DELIMITER} string list
     * @see RegExGenerator#joinListToRegEx(List)
     */
    public static String joinListToRegEx(RegExGenerator<?> regexGenerator, List<String> elList){
        StringBuilder builder = new StringBuilder();

        boolean isNonCapturingGroup = regexGenerator == null || regexGenerator.isNonCapturingGroup;

        builder.append(isNonCapturingGroup ? "(?:" : '(');

        Iterator<String> iterator = elList.iterator();
        while (iterator.hasNext()){
            builder.append(iterator.next());
            if (iterator.hasNext()){
                builder.append(ELEMENT_DELIMITER);
            }
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public SettingsOption getSettingsOption() {
        return this.settingsOption;
    }

    @Override
    public void setNonCapturingGroup(boolean capturingGroup) {
        this.isNonCapturingGroup = capturingGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isNonCapturingGroup, this.getSettingsOption());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        try{
            RegExGenerator<T> that = (RegExGenerator<T>) o;
            return this.isNonCapturingGroup == that.isNonCapturingGroup && this.getSettingsOption() == that.getSettingsOption();
        } catch (ClassCastException e){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, "Error while casting RegExGenerator: {0}", e);
        }
        return false;
    }
}
