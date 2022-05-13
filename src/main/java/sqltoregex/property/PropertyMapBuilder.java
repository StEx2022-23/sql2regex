package sqltoregex.property;

import org.apache.commons.lang3.tuple.Pair;
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
    private final Map<PropertyOption, Property<?>> propertyMap;
    private final Set<SpellingMistake> spellingMistakes;

    public PropertyMapBuilder() {
        this.propertyMap = new EnumMap<>(PropertyOption.class);
        this.orderRotations = new LinkedHashSet<>();
        this.spellingMistakes = new LinkedHashSet<>();
    }

    public Map<PropertyOption, Property<?>> build() {
        for (OrderRotation orderRotation : orderRotations) {
            for (PropertyOption propertyOption : orderRotation.getSettings()){
                SpellingMistake spellingMistake = new SpellingMistake(PropertyOption.valueOf(propertyOption.toString().substring(0, propertyOption.toString().length()-5) + "SPELLING"));
                if (spellingMistakes.contains(spellingMistake)){
                    orderRotation.setSpellingMistake(spellingMistake);
                }
            }
        }
        return this.propertyMap;
    }

    public PropertyMapBuilder with(Set<String> synonyms, PropertyOption propertyOption) {
        switch (propertyOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(propertyOption);
                for (String format : synonyms) {
                    synonymGenerator.addSynonym(new SimpleDateFormat(format));
                }
                this.propertyMap.put(propertyOption, synonymGenerator);
            }
            case AGGREGATEFUNCTIONLANG -> {
                StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(propertyOption);
                for(String singleSynonym : synonyms){
                    aggregateFunctionSynonymGenerator.addSynonymFor(singleSynonym.split(",")[0], singleSynonym.split(",")[1]);
                }
                this.propertyMap.put(propertyOption, aggregateFunctionSynonymGenerator);
            } default -> throw new IllegalArgumentException("Unsupported build with:" + propertyOption);
        }
        return this;
    }

    public PropertyMapBuilder with(PropertyOption propertyOption) {
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
            } default -> throw new IllegalArgumentException("Unsupported build with:" + propertyOption);
        }
        return this;
    }
}
