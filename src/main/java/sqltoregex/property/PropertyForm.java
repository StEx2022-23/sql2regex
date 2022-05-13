package sqltoregex.property;

import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Set;

public record PropertyForm(@NotNull Set<PropertyOption> spellings,
                           @NotNull Set<PropertyOption> orders,
                           @NotNull Set<SimpleDateFormat> dateFormats,
                           @NotNull Set<SimpleDateFormat> timeFormats,
                           @NotNull Set<SimpleDateFormat> dateTimeFormats,
                           @NotNull Pair<String, String> sumSynonym,
                           @NotNull Pair<String, String> avgSynonym,
                           @NotEmpty String sql) {

    public Pair<String, String> getSumSynonym() {
        return sumSynonym;
    }

    public Pair<String, String> getAvgSynonym() {
        return avgSynonym;
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
