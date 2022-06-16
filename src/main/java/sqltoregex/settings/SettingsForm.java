package sqltoregex.settings;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;

/**
 * SettingsForm is the connection between front- and backend. In this class is the sent form data's stored.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
public class SettingsForm {

    /**
     * Realize an empty form SettingsForm to enable specific testing options.
     */
    public static final SettingsForm EMPTY_FORM = new SettingsForm(Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   Collections.emptySet(),
                                                                   "EMPTY",
                                                            "EMPTY");
    Set<SettingsOption> spellings;
    Set<SettingsOption> orders;
    Set<SimpleDateFormat> dateFormats;
    Set<SimpleDateFormat> timeFormats;
    Set<SimpleDateFormat> dateTimeFormats;
    Set<String> aggregateFunctionLang;
    Set<String> datatypeSynonyms;
    Set<String> otherSynonyms;
    @NotEmpty(message = "{settingsForm.sql.NotEmpty}") String sql;
    String validation;

    /**
     * Constructor for the SettingsForm.
     * @param spellings all spelling settings as Set of {@link SettingsOption}
     * @param orders all orders settings as Set of {@link SettingsOption}
     * @param dateFormats all dateFormats settings as Set of {@link SimpleDateFormat}
     * @param timeFormats all timeFormats settings as Set of {@link SimpleDateFormat}
     * @param dateTimeFormats all dateTimeFormats settings as Set of {@link SimpleDateFormat}
     * @param aggregateFunctionLang All aggregateFunctionLanguage settings as String-Set
     * @param datatypeSynonyms all datatypeSynonyms settings as String-Set
     * @param otherSynonyms all otherSynonyms settings as String-Set
     * @param sql the given SQL-Statement as String
     */
    public SettingsForm(Set<SettingsOption> spellings,
                        Set<SettingsOption> orders,
                        Set<SimpleDateFormat> dateFormats,
                        Set<SimpleDateFormat> timeFormats,
                        Set<SimpleDateFormat> dateTimeFormats,
                        Set<String> aggregateFunctionLang,
                        Set<String> datatypeSynonyms,
                        Set<String> otherSynonyms,
                        String sql,
                        String validation) {
        this.spellings = spellings == null ? Collections.emptySet() : spellings;
        this.orders = orders == null ? Collections.emptySet() : orders;
        this.dateFormats = dateFormats == null ? Collections.emptySet() : dateFormats;
        this.timeFormats = timeFormats == null ? Collections.emptySet() : timeFormats;
        this.dateTimeFormats = dateTimeFormats == null ? Collections.emptySet() : dateTimeFormats;
        this.aggregateFunctionLang = aggregateFunctionLang == null ? Collections.emptySet() : aggregateFunctionLang;
        this.datatypeSynonyms= datatypeSynonyms == null ? Collections.emptySet() : datatypeSynonyms;
        this.otherSynonyms = otherSynonyms == null ? Collections.emptySet() : otherSynonyms;
        this.sql = sql;
        this.validation = validation;
    }

    public Set<String> getAggregateFunctionLang() {
        return this.aggregateFunctionLang;
    }

    public Set<String> getDatatypeSynonyms() {
        return this.datatypeSynonyms;
    }

    public Set<String> getOtherSynonyms() {
        return this.otherSynonyms;
    }

    public Set<SimpleDateFormat> getDateFormats() {
        return this.dateFormats;
    }

    public Set<SimpleDateFormat> getDateTimeFormats() {
        return this.dateTimeFormats;
    }

    public Set<SettingsOption> getOrders() {
        return this.orders;
    }

    public Set<SettingsOption> getSpellings() {
        return this.spellings;
    }

    public String getSql() {
        return this.sql;
    }

    public Set<SimpleDateFormat> getTimeFormats() {
        return this.timeFormats;
    }

    public String getValidation() {
        return this.validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }
}
