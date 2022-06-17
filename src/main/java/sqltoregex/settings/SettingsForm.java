package sqltoregex.settings;

import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import sqltoregex.settings.validations.AssertMethodAsTrue;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SettingsForm is the connection between front- and backend. In this class is the sent form data's stored.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
@AssertMethodAsTrue(value="validateSQL", message = "{settingsform.sqlInput.invalidSql}", field = "sql" )
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
                                                                   "EMPTY");
    Set<SettingsOption> spellings;
    Set<SettingsOption> orders;
    Set<SimpleDateFormat> dateFormats;
    Set<SimpleDateFormat> timeFormats;
    Set<SimpleDateFormat> dateTimeFormats;
    Set<String> aggregateFunctionLang;
    Set<String> datatypeSynonyms;
    Set<String> otherSynonyms;
    @NotEmpty(message = "{settingsForm.sql.notEmpty}") String sql;

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
                        @NotEmpty(message = "{settingsForm.sql.NotEmpty}") String sql) {
        this.spellings = spellings == null ? Collections.emptySet() : spellings;
        this.orders = orders == null ? Collections.emptySet() : orders;
        this.dateFormats = dateFormats == null ? Collections.emptySet() : dateFormats;
        this.timeFormats = timeFormats == null ? Collections.emptySet() : timeFormats;
        this.dateTimeFormats = dateTimeFormats == null ? Collections.emptySet() : dateTimeFormats;
        this.aggregateFunctionLang = aggregateFunctionLang == null ? Collections.emptySet() : aggregateFunctionLang;
        this.datatypeSynonyms= datatypeSynonyms == null ? Collections.emptySet() : datatypeSynonyms;
        this.otherSynonyms = otherSynonyms == null ? Collections.emptySet() : otherSynonyms;
        this.sql = sql;
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

    /**
     * Validates a statements against oracle, mysql, sqlserver or mariadb grammar.
     * @return boolean if the statement is valid
     */
    public boolean validateSQL() {
        Map<Validation, Boolean> validationList = new HashMap<>();
        validationList.put(new Validation(List.of(DatabaseType.ORACLE), this.sql), false);
        validationList.put(new Validation(List.of(DatabaseType.MYSQL), this.sql), false);
        validationList.put(new Validation(List.of(DatabaseType.SQLSERVER), this.sql), false);
        validationList.put(new Validation(List.of(DatabaseType.MARIADB), this.sql), false);

        for(Validation validation : validationList.keySet()){
            List<ValidationError> validationErrors = validation.validate();
            if (!validationErrors.isEmpty()) {
                for (ValidationError va : validationErrors) {
                    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                    logger.log(Level.WARNING, "Error while validating the statement: {0}", va);
                }
            } else {
                validationList.put(validation, true);
            }
        }

        for (Map.Entry<Validation, Boolean> entry : validationList.entrySet()) {
            if(entry.getValue().equals(Boolean.TRUE)) {
                return true;
            }
        }

        if (this.sql.contains("CREATE") && this.sql.contains("DATABASE")){
            return true;
        }

        return false;
    }
}
