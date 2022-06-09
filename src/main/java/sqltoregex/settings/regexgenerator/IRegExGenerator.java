package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.SettingsOption;
import java.util.List;

/**
 * The RegExGenerator interface defines methods for use in the respective deparser.
 * The implementation takes place in the respective interface implementations.
 * @param <T> String, List of String or SimpleDateFormat
 */
public interface IRegExGenerator<T> {
    /**
     * Generate a regex string.
     * @param input  String, List of String or SimpleDateFormat
     * @return generated regex
     */
    String generateRegExFor(T input);

    /**
     * Generate a List of strings with the specific behave of the regex generator.
     * @param input  String, List of String or SimpleDateFormat
     * @return generated list of regex behave for the respective regex generator
     */
    List<String> generateAsList(T input);

    /**
     * Gives the specified SettingsOption. One of enum {@link SettingsOption}.
     * @return one of enum {@link SettingsOption}
     * @see SettingsOption
     */
    SettingsOption getSettingsOption();

    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    void setNonCapturingGroup(boolean capturingGroup);
}
