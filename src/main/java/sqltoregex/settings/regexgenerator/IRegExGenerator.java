package sqltoregex.settings.regexgenerator;

import sqltoregex.settings.SettingsOption;
import java.util.List;

public interface IRegExGenerator<T> {
    String generateRegExFor(T input);

    List<String> generateAsList(T input);

    SettingsOption getSettingsOption();

    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     *
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    void setNonCapturingGroup(boolean capturingGroup);
}
