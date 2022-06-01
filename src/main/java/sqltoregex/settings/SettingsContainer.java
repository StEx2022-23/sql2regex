package sqltoregex.settings;

import sqltoregex.settings.regexgenerator.GroupByElementRotation;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class SettingsContainer {
    SingleSettingsContainer<SpellingMistake> spellingMistakes = new SingleSettingsContainer<>();
    SingleSettingsContainer<OrderRotation> orderRotations = new SingleSettingsContainer<>();
    SingleSettingsContainer<StringSynonymGenerator> stringSynonymGenerators = new SingleSettingsContainer<>();
    SingleSettingsContainer<DateAndTimeFormatSynonymGenerator> dateAndTimeFormatSynonymGenerators = new SingleSettingsContainer<>();
    SingleSettingsContainer<GroupByElementRotation> groupByElementRotations = new SingleSettingsContainer<>();

    public class SingleSettingsContainer<V extends IRegExGenerator<?>> extends EnumMap<SettingsOption,V>{

        SingleSettingsContainer(){
            super(SettingsOption.class);
        }

        V get(SettingsOption settingsOption){
            return getOrDefault(settingsOption, null);
        }
    }

    public SettingsContainer withOrderRotation(OrderRotation orderRotation){
       orderRotations.put(orderRotation.getSettingsOption(), orderRotation);
        return this;
    }

    public SettingsContainer withSpellingMistake(SpellingMistake spellingMistake){
        this.spellingMistakes.put(spellingMistake.getSettingsOption(), spellingMistake);
        return this;
    }

    public SettingsContainer withStringSynonymGenerator(StringSynonymGenerator synonymGenerator){
        stringSynonymGenerators.put(synonymGenerator.getSettingsOption(), synonymGenerator);
        return this;
    }

    public SettingsContainer withDateAndTimeSynonymGenerator(DateAndTimeFormatSynonymGenerator synonymGenerator){
        dateAndTimeFormatSynonymGenerators.put(synonymGenerator.getSettingsOption(), synonymGenerator);
        return this;
    }

    public SettingsContainer withAllSpellingMistakesAndOrderRotations(){
        for(SettingsOption settingsOption : SettingsOption.values()){
            switch (settingsOption){
                case COLUMNNAMEORDER, TABLENAMEORDER, GROUPBYELEMENTORDER -> {
                    this.withOrderRotation(new OrderRotation(settingsOption));
                }
                case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                    this.withSpellingMistake(new SpellingMistake(settingsOption));
                }
            }
        }
        return this;
    }

    public SettingsContainer withAllSpellingMistakes(){
        for(SettingsOption settingsOption : SettingsOption.values()){
            switch (settingsOption){
                case COLUMNNAMEORDER, TABLENAMEORDER, GROUPBYELEMENTORDER -> {
                    this.withOrderRotation(new OrderRotation(settingsOption));
                }
            }
        }
        return this;
    }

    public SettingsContainer withAllOrderRotations(){
        for(SettingsOption settingsOption : SettingsOption.values()){
            switch (settingsOption){
                case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                    this.withSpellingMistake(new SpellingMistake(settingsOption));
                }
            }
        }
        return this;
    }

    public SettingsContainer withSettingsManager(SettingsManager settingsManager){
        for (OrderRotation orderRotation : settingsManager.getSettingByClass(OrderRotation.class)){
            this.withOrderRotation(orderRotation);
        }
        for (SpellingMistake spellingMistake : settingsManager.getSettingByClass(SpellingMistake.class)){
            this.withSpellingMistake(spellingMistake);
        }
        for (StringSynonymGenerator synonymGenerator : settingsManager.getSettingByClass(StringSynonymGenerator.class)){
            this.withStringSynonymGenerator(synonymGenerator);
        }
        for (DateAndTimeFormatSynonymGenerator synonymGenerator : settingsManager.getSettingByClass(DateAndTimeFormatSynonymGenerator.class)){
            this.withDateAndTimeSynonymGenerator(synonymGenerator);
        }
        return this;
    }

    public <C extends IRegExGenerator<?>> SingleSettingsContainer<C> get(Class<C> clazz){
        try {
            if (OrderRotation.class.isAssignableFrom(clazz)) {
                return castSingleSettingsContainer (this.orderRotations, clazz);
            }else if (GroupByElementRotation.class.isAssignableFrom(clazz)){
                return castSingleSettingsContainer (this.groupByElementRotations, clazz);
            } else if (SpellingMistake.class.isAssignableFrom(clazz)) {
                return castSingleSettingsContainer (this.spellingMistakes, clazz);
            } else if (SynonymGenerator.class.isAssignableFrom(clazz) ) {
                return castSingleSettingsContainer (this.stringSynonymGenerators, clazz);
            }else{
                return null;
            }
        } catch (ClassCastException e){ return null;}
    }

    private <T extends IRegExGenerator<?>> SingleSettingsContainer<T> castSingleSettingsContainer(SingleSettingsContainer<?> singleSettingsContainer, Class<T> clazz){
        SingleSettingsContainer<T> newContainer = new SingleSettingsContainer<>();

        for (Map.Entry<SettingsOption,?> entry : singleSettingsContainer.entrySet()){
            try{
                newContainer.put(entry.getKey(), clazz.cast(entry.getValue()));
            }catch (ClassCastException e){
                //continue trying to cast other values
            }
        }
        return newContainer;
    }

    public Map<SettingsOption, SpellingMistake> getSpellingMistakes(){
        return this.spellingMistakes;
    }

    public Map<SettingsOption, OrderRotation> getOrderRotations(){
        return this.orderRotations;
    }

    public SingleSettingsContainer<StringSynonymGenerator> getStringSynonymGenerators() {
        return stringSynonymGenerators;
    }

    public SingleSettingsContainer<DateAndTimeFormatSynonymGenerator> getDateAndTimeFormatSynonymGenerators() {
        return dateAndTimeFormatSynonymGenerators;
    }
}
