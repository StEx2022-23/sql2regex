package sqltoregex.property;

import org.springframework.util.Assert;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PropertyMapBuilder {
    private final Set<OrderRotation> orderRotations;
    private final Map<PropertyOption, RegExGenerator<?, ?>> propertyMap;
    private final Set<SpellingMistake> spellingMistakes;
    private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
    private static final String STRING_SYNONYM_DELIMITER = ";";

    public PropertyMapBuilder() {
        this.propertyMap = new EnumMap<>(PropertyOption.class);
        this.orderRotations = new LinkedHashSet<>();
        this.spellingMistakes = new LinkedHashSet<>();
    }

    public Map<PropertyOption, RegExGenerator<?, ?>> build() {
        for (OrderRotation orderRotation : orderRotations) {
            for (PropertyOption propertyOption : orderRotation.getSettings()) {
                SpellingMistake spellingMistake = new SpellingMistake(PropertyOption.valueOf(
                        propertyOption.toString().substring(0, propertyOption.toString().length() - 5) + "SPELLING"));
                if (spellingMistakes.contains(spellingMistake)) {
                    orderRotation.setSpellingMistake(spellingMistake);
                }
            }
        }
        return this.propertyMap;
    }

    public PropertyMapBuilder withPropertyOption(PropertyOption propertyOption) {
        switch (propertyOption) {
            case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                SpellingMistake spellingMistake = new SpellingMistake(propertyOption);
                this.propertyMap.put(propertyOption, spellingMistake);
                spellingMistakes.add(spellingMistake);
            }
            case TABLENAMEORDER, COLUMNNAMEORDER -> {
                OrderRotation orderRotation = new OrderRotation(propertyOption);
                this.propertyMap.put(propertyOption, orderRotation);
                orderRotations.add(orderRotation);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + propertyOption);
        }
        return this;
    }

    public PropertyMapBuilder withPropertyOptionSet(Set<PropertyOption> propertyOptions) {
        Assert.notNull(propertyOptions, "Set of property options must not be null");
        for (PropertyOption propertyOption : propertyOptions) {
            withPropertyOption(propertyOption);
        }
        return this;
    }

    public PropertyMapBuilder withSimpleDateFormatSet(Set<SimpleDateFormat> synonyms, PropertyOption propertyOption) {
        Assert.notNull(synonyms, "Set of simple date formats options must not be null");
        switch (propertyOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                        propertyOption);
                for (SimpleDateFormat format : synonyms) {
                    synonymGenerator.addSynonym(format);
                }
                this.propertyMap.put(propertyOption, synonymGenerator);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + propertyOption);
        }
        return this;
    }

    public PropertyMapBuilder withStringSet(Set<String> synonyms, PropertyOption propertyOption) {
        Assert.notNull(synonyms, "Set of strings options must not be null");
            switch (propertyOption) {
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(propertyOption);
                    for (String format : synonyms) {
                        synonymGenerator.addSynonym(new SimpleDateFormat(format));
                    }
                    this.propertyMap.put(propertyOption, synonymGenerator);
                }
                case AGGREGATEFUNCTIONLANG -> {
                    if (!synonyms.isEmpty()) {
                        StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(propertyOption);
                        for (String singleSynonym : synonyms) {
                            aggregateFunctionSynonymGenerator.addSynonymFor(singleSynonym.split(STRING_SYNONYM_DELIMITER)[0].strip(),
                                                                            singleSynonym.split(STRING_SYNONYM_DELIMITER)[1].strip());
                        }
                        this.propertyMap.put(propertyOption, aggregateFunctionSynonymGenerator);
                    }
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + propertyOption);
            }
        return this;
    }
}
