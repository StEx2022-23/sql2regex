package sqltoregex.property;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Set;

public record SettingsForm(@NotNull Set<SettingsOption> spellings,
                           @NotNull Set<SettingsOption> orders,
                           @NotNull Set<SimpleDateFormat> dateFormats,
                           @NotNull Set<SimpleDateFormat> timeFormats,
                           @NotNull Set<SimpleDateFormat> dateTimeFormats,
                           @NotNull Set<String> aggregateFunctionLang,
                           @NotEmpty String sql) {

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
