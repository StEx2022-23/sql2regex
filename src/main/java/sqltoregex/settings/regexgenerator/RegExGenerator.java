package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class to specify implementation of the regex generators.
 * @param <T> String, List of String or SimpleDateFormat
 */
public abstract class RegExGenerator<T> implements IRegExGenerator<T> {
    protected static final String ELEMENT_DELIMITER = "|";
    protected boolean isNonCapturingGroup = true;
    private final SettingsOption settingsOption;

    /**
     * Constructor of RegExGenerator. Need to set one of enum {@link SettingsOption}.
     * @param settingsOption one of enum {@link SettingsOption}
     */
    protected RegExGenerator(SettingsOption settingsOption){
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.settingsOption = settingsOption;
    }

    /**
     * Generates a regular expression part String with the pre-/ and suffixes set <b>including</b> the param.
     * @param value value to handle
     * @return generated RegEx
     */
    public String generateRegExFor(T value) {
        return this.joinListToRegEx(this.generateAsList(value));
    }

    /**
     * Generates a regex like (?:obj1|obj2|ob3) from a List of String.
     * @param elList list of string, with alternative options
     * @return generated regex
     */
    public String joinListToRegEx(List<String> elList){
        return RegExGenerator.joinListToRegEx(this, elList);
    }

    /**
     * For joining Lists in a static context. Extracting {@link RegExGenerator#isNonCapturingGroup} from the Parameter for right parsing.
     * @param regexGenerator object instance of {@link IRegExGenerator}
     * @param elList list of strings that should be present in the regex
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

    /**
     * Implements method from {@link IRegExGenerator}.
     * @return one of enum {@link SettingsOption}
     * @see IRegExGenerator
     */
    @Override
    public SettingsOption getSettingsOption() {
        return this.settingsOption;
    }

    /**
     * Defines if the generated regex is a capturing group (obj1|obj2) or a non-capturing group (?:obj1|obj2).
     * @param nonCapturingGroup false for capturing group and true for non-capturing group
     * @see IRegExGenerator
     */
    @Override
    public void setNonCapturingGroup(boolean nonCapturingGroup) {
        this.isNonCapturingGroup = nonCapturingGroup;
    }

    /**
     * Overrides default hashCode() method. The object hash is related to the capturing group setting and the {@link SettingsOption}.
     * @return hashcode as int
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.isNonCapturingGroup, this.getSettingsOption());
    }

    /**
     * Overrides default equals() method. Two objects are equal, if the capturing group setting and the {@link SettingsOption} are equal.
     * @param o to compare object
     * @return boolean if equal
     */
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
