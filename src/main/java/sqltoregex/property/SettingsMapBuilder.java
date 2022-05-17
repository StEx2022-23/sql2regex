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

public class SettingsMapBuilder {
    private final Set<OrderRotation> orderRotations;
    private final Map<SettingsOption, RegExGenerator<?, ?>> propertyMap;
    private final Set<SpellingMistake> spellingMistakes;
    private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
    private static final String STRING_SYNONYM_DELIMITER = ";";

    public SettingsMapBuilder() {
        this.propertyMap = new EnumMap<>(SettingsOption.class);
        this.orderRotations = new LinkedHashSet<>();
        this.spellingMistakes = new LinkedHashSet<>();
    }

    public Map<SettingsOption, RegExGenerator<?, ?>> build() {
        for (OrderRotation orderRotation : orderRotations) {
            for (SettingsOption settingsOption : orderRotation.getSettings()) {
                SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.valueOf(
                        settingsOption.toString().substring(0, settingsOption.toString().length() - 5) + "SPELLING"));
                if (spellingMistakes.contains(spellingMistake)) {
                    orderRotation.setSpellingMistake(spellingMistake);
                }
            }
        }
        return this.propertyMap;
    }

    public SettingsMapBuilder withPropertyOption(SettingsOption settingsOption) {
        switch (settingsOption) {
            case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                SpellingMistake spellingMistake = new SpellingMistake(settingsOption);
                this.propertyMap.put(settingsOption, spellingMistake);
                spellingMistakes.add(spellingMistake);
            }
            case TABLENAMEORDER, COLUMNNAMEORDER -> {
                OrderRotation orderRotation = new OrderRotation(settingsOption);
                this.propertyMap.put(settingsOption, orderRotation);
                orderRotations.add(orderRotation);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withPropertyOptionSet(Set<SettingsOption> settingsOptions) {
        Assert.notNull(settingsOptions, "Set of property options must not be null");
        for (SettingsOption settingsOption : settingsOptions) {
            withPropertyOption(settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withSimpleDateFormatSet(Set<SimpleDateFormat> synonyms, SettingsOption settingsOption) {
        Assert.notNull(synonyms, "Set of simple date formats options must not be null");
        switch (settingsOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                        settingsOption);
                for (SimpleDateFormat format : synonyms) {
                    synonymGenerator.addSynonym(format);
                }
                this.propertyMap.put(settingsOption, synonymGenerator);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withStringSet(Set<String> synonyms, SettingsOption settingsOption) {
        Assert.notNull(synonyms, "Set of strings options must not be null");
            switch (settingsOption) {
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                            settingsOption);
                    for (String format : synonyms) {
                        synonymGenerator.addSynonym(new SimpleDateFormat(format));
                    }
                    this.propertyMap.put(settingsOption, synonymGenerator);
                }
                case AGGREGATEFUNCTIONLANG -> {
                    if (!synonyms.isEmpty()) {
                        StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(
                                settingsOption);
                        for (String singleSynonym : synonyms) {
                            aggregateFunctionSynonymGenerator.addSynonymFor(singleSynonym.split(STRING_SYNONYM_DELIMITER)[0].strip(),
                                                                            singleSynonym.split(STRING_SYNONYM_DELIMITER)[1].strip());
                        }
                        this.propertyMap.put(settingsOption, aggregateFunctionSynonymGenerator);
                    }
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
        return this;
    }
}
