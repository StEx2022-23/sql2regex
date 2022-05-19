package sqltoregex.settings;

public interface RegExGenerator<S, R> {
    String generateRegExFor(R input);

    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    void setCapturingGroup(boolean capturingGroup);

    SettingsOption getSettingsOption();
}
