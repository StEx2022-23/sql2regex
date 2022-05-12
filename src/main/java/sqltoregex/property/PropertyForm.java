package sqltoregex.property;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Set;

public class PropertyForm {

    @NotNull
    private final Set<String> aggregateFuncLang;
    @NotNull
    private final Set<SimpleDateFormat> dateFormats;
    @NotNull
    private final Set<SimpleDateFormat> dateTimeFormats;
    @NotNull
    private final Set<PropertyOption> orders;
    @NotNull
    private final Set<PropertyOption> spellings;
    @NotEmpty
    private final String sql;
    @NotNull
    private final Set<SimpleDateFormat> timeFormats;

    public PropertyForm(Set<PropertyOption> spellings, Set<PropertyOption> orders, Set<SimpleDateFormat> dateFormats,
                        Set<SimpleDateFormat> timeFormats, Set<SimpleDateFormat> dateTimeFormats,
                        Set<String> aggregateFuncLang, String sql) {
        this.spellings = spellings;
        this.orders = orders;
        this.dateFormats = dateFormats;
        this.timeFormats = timeFormats;
        this.dateTimeFormats = dateTimeFormats;
        this.aggregateFuncLang = aggregateFuncLang;
        this.sql = sql;
    }

    public Set<String> getAggregateFuncLang() {
        return aggregateFuncLang;
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

    ;
}
