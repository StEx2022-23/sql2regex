package sqltoregex.settings;

import javax.validation.constraints.NotEmpty;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;

public class SettingsForm {

    Set<SettingsOption> spellings;
    Set<SettingsOption> orders;
    Set<SimpleDateFormat> dateFormats;
    Set<SimpleDateFormat> timeFormats;
    Set<SimpleDateFormat> dateTimeFormats;
    Set<String> aggregateFunctionLang;
    @NotEmpty(message = "{settingsForm.sql.NotEmpty}") String sql;

    public SettingsForm(Set<SettingsOption> spellings,
                        Set<SettingsOption> orders,
                        Set<SimpleDateFormat> dateFormats,
                        Set<SimpleDateFormat> timeFormats,
                        Set<SimpleDateFormat> dateTimeFormats,
                        Set<String> aggregateFunctionLang,
                        String sql){
        this.spellings = spellings == null ? Collections.emptySet() : spellings;
        this.orders = orders == null ? Collections.emptySet() :orders;
        this.dateFormats = dateFormats == null ? Collections.emptySet() :dateFormats;
        this.timeFormats = timeFormats == null ? Collections.emptySet() :timeFormats;
        this.dateTimeFormats = dateTimeFormats == null ? Collections.emptySet() :dateTimeFormats;
        this.aggregateFunctionLang = aggregateFunctionLang == null ? Collections.emptySet() :aggregateFunctionLang;
        this.sql = sql;
    }

    public Set<String> getAggregateFunctionLang() {
        return aggregateFunctionLang;
    }

    public Set<SimpleDateFormat> getDateFormats() {
        return dateFormats;
    }

    public Set<SimpleDateFormat> getDateTimeFormats() {
        return dateTimeFormats;
    }

    public Set<SettingsOption> getOrders() {
        return orders;
    }

    public Set<SettingsOption> getSpellings() {
        return spellings;
    }

    public String getSql() {
        return sql;
    }

    public Set<SimpleDateFormat> getTimeFormats() {
        return timeFormats;
    }
    }
