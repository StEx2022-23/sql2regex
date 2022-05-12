package sqltoregex.property;

import java.text.SimpleDateFormat;
import java.util.*;


public class PropertyMapBuilder {
    private final Map<PropertyOption, Property<?>> propertyMap;

    public PropertyMapBuilder() {
        this.propertyMap = new EnumMap<>(PropertyOption.class);
    }

    public PropertyMapBuilder with(PropertyOption propertyOption) {
        switch (propertyOption){
            case KEYWORDSPELLING -> this.propertyMap.put(propertyOption, new SpellingMistake(propertyOption));
            case TABLENAMEORDER, COLUMNNAMEORDER -> this.propertyMap.put(propertyOption, new OrderRotation(propertyOption));
        }
        return this;
    }

    public PropertyMapBuilder with(Set<String> formats, PropertyOption propertyOption) {
        switch (propertyOption){
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator();
                for (String format : formats) {
                    synonymGenerator.addSynonym(new SimpleDateFormat(format));
                }
                this.propertyMap.put(propertyOption, synonymGenerator);
            }
            default -> {
                DefaultSynonymGenerator synonymGenerator = new DefaultSynonymGenerator();
                for (String format : formats) {
                    synonymGenerator.addSynonym(format);
                }
                this.propertyMap.put(propertyOption, synonymGenerator);
            }
        }
        return this;
    }

    public Map<PropertyOption, Property<?>> build() {
        return this.propertyMap;
    }
}
