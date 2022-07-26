package sqltoregex.settings;

import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import sqltoregex.ConverterManagement;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.validations.AssertMethodAsTrue;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                                                                   Collections.emptySet(),
                                                                   "EMPTY");
    private final Set<SpellingMistake> spellings;
    private final Set<OrderRotation> orders;
    private final Set<SimpleDateFormat> dateFormats;
    private final Set<SimpleDateFormat> timeFormats;
    private final Set<SimpleDateFormat> dateTimeFormats;
    private final Set<String> aggregateFunctionLang;
    private final Set<String> functionLang;
    private final Set<String> datatypeSynonyms;
    private final Set<String> otherSynonyms;
    @NotEmpty(message = "{settingsform.sql.notEmpty}")
    private final String sql;

    /**
     * Constructor for the SettingsForm.
     * @param spellings all spelling settings as Set of {@link SpellingMistake}
     * @param orders all orders settings as Set of {@link OrderRotation}
     * @param dateFormats all dateFormats settings as Set of {@link SimpleDateFormat}
     * @param timeFormats all timeFormats settings as Set of {@link SimpleDateFormat}
     * @param dateTimeFormats all dateTimeFormats settings as Set of {@link SimpleDateFormat}
     * @param aggregateFunctionLang All aggregateFunctionLanguage settings as String-Set
     * @param datatypeSynonyms all datatypeSynonyms settings as String-Set
     * @param otherSynonyms all otherSynonyms settings as String-Set
     * @param sql the given SQL-Statement as String
     */
    public SettingsForm(Set<SpellingMistake> spellings,
                        Set<OrderRotation> orders,
                        Set<SimpleDateFormat> dateFormats,
                        Set<SimpleDateFormat> timeFormats,
                        Set<SimpleDateFormat> dateTimeFormats,
                        Set<String> aggregateFunctionLang,
                        Set<String> functionLang,
                        Set<String> datatypeSynonyms,
                        Set<String> otherSynonyms,
                        @NotEmpty(message = "{settingsform.sql.NotEmpty}") String sql) {
        this.spellings = spellings == null ? Collections.emptySet() : spellings;
        this.orders = orders == null ? Collections.emptySet() : orders;
        this.dateFormats = dateFormats == null ? Collections.emptySet() : setAllNonLenient(dateFormats);
        this.timeFormats = timeFormats == null ? Collections.emptySet() : setAllNonLenient(timeFormats);
        this.dateTimeFormats = dateTimeFormats == null ? Collections.emptySet() : setAllNonLenient(dateTimeFormats);
        this.aggregateFunctionLang = aggregateFunctionLang == null ? Collections.emptySet() : aggregateFunctionLang;
        this.functionLang = functionLang == null ? Collections.emptySet() : functionLang;
        this.datatypeSynonyms= datatypeSynonyms == null ? Collections.emptySet() : datatypeSynonyms;
        this.otherSynonyms = otherSynonyms == null ? Collections.emptySet() : otherSynonyms;
        this.sql = sql;
    }

    public Set<String> getAggregateFunctionLang() {
        return this.aggregateFunctionLang;
    }

    public Set<String> getFunctionLang() {
        return this.functionLang;
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

    public Set<OrderRotation> getOrders() {
        return this.orders;
    }

    public Set<SpellingMistake> getSpellings() {
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
        Map<String, Boolean> statementsValidMap = new HashMap<>();
        Collection<String> extractedStatementCollection = ConverterManagement.extractStatements(this.sql);
        for(String str : extractedStatementCollection) {validationStep(str, statementsValidMap);}

        return statementsValidMap.size() == extractedStatementCollection.size();
    }

    /**
     * Sets the provided Set of SimpleDateFormats to lenient false
     * @param formats
     * @return
     */
    private Set<SimpleDateFormat> setAllNonLenient(Set<SimpleDateFormat> formats){
        formats.forEach(format -> format.setLenient(false));
        return formats;
    }

    private void validationStep(String str, Map<String, Boolean> statementsValidMap){
        List<Validation> validationList = new LinkedList<>();
        validationList.add(new Validation(List.of(DatabaseType.ORACLE), str));
        validationList.add(new Validation(List.of(DatabaseType.MYSQL), str));
        validationList.add(new Validation(List.of(DatabaseType.SQLSERVER), str));
        validationList.add(new Validation(List.of(DatabaseType.MARIADB), str));

        for(Validation validation : validationList){
            List<ValidationError> validationErrors = validation.validate();
            if (!validationErrors.isEmpty()) {
                Pattern pattern = Pattern.compile("CREATE DATABASE [`´'\"]?(\\w*)[`´'\"]?( .*)*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()){
                    statementsValidMap.put(str, true);
                    return;
                }

                for (ValidationError va : validationErrors) {
                    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                    logger.log(Level.WARNING, "Error while validating the statement: {0}", va);
                }
            } else {
                statementsValidMap.put(str, true);
            }
        }
    }
}
