package sqltoregex.property;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Set;

public record PropertyForm(@NotNull Set<PropertyOption> spellings,
                           @NotNull Set<PropertyOption> orders,
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

    public Set<PropertyOption> getOrders() {
        return orders;
    }

    public Set<PropertyOption> getSpellings() {
        return spellings;
    }

    public String getSql() {
        return sql;
    }

    public Set<SimpleDateFormat> getTimeFormats() {
        return timeFormats;
    }



}
