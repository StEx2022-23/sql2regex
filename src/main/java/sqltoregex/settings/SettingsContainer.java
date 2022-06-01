package sqltoregex.settings;

import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.util.EnumMap;
import java.util.Map;

public class SettingsContainer {
    SettingsMap<IRegExGenerator<?>> allSettings = new SettingsMap<>();

    public void putAll(SettingsContainer settingsContainer) {
        allSettings.putAll(settingsContainer.allSettings);
    }

    public SettingsContainer with(IRegExGenerator<?> regExGenerator){
        this.allSettings.put(regExGenerator.getSettingsOption(), regExGenerator);
        return this;
    }

    public SettingsContainer withAllSpellingMistakesAndOrderRotations(){
        for(SettingsOption settingsOption : SettingsOption.values()){
            switch (settingsOption){
                case COLUMNNAMEORDER, TABLENAMEORDER, GROUPBYELEMENTORDER -> {
                    this.with(new OrderRotation(settingsOption));
                }
                case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                    this.with(new SpellingMistake(settingsOption));
                }
            }
        }
        return this;
    }

    public SettingsContainer withSettingsManager(SettingsManager settingsManager){
        for (OrderRotation orderRotation : settingsManager.getSettingByClass(OrderRotation.class)){
            this.with(orderRotation);
        }
        for (SpellingMistake spellingMistake : settingsManager.getSettingByClass(SpellingMistake.class)){
            this.with(spellingMistake);
        }
        for (StringSynonymGenerator synonymGenerator : settingsManager.getSettingByClass(StringSynonymGenerator.class)){
            this.with(synonymGenerator);
        }
        for (DateAndTimeFormatSynonymGenerator synonymGenerator : settingsManager.getSettingByClass(DateAndTimeFormatSynonymGenerator.class)){
            this.with(synonymGenerator);
        }
        return this;
    }

    public IRegExGenerator<?> get(SettingsOption settingsOption){
        for (IRegExGenerator<?> generator : this.allSettings.values()){
            if (generator.getSettingsOption() == settingsOption){
                return generator;
            }
        }
        return null;
    }

    public <C extends IRegExGenerator<?>> SettingsMap<C> get(Class<C> clazz){
        try {
            if (OrderRotation.class.isAssignableFrom(clazz)) {
                return castSingleSettingsContainer (this.allSettings, clazz);
            }else if (OrderRotation.class.isAssignableFrom(clazz)){
                return castSingleSettingsContainer (this.allSettings, clazz);
            } else if (SpellingMistake.class.isAssignableFrom(clazz)) {
                return castSingleSettingsContainer (this.allSettings, clazz);
            } else if (SynonymGenerator.class.isAssignableFrom(clazz) ) {
                return castSingleSettingsContainer (this.allSettings, clazz);
            }else{
                return null;
            }
        } catch (ClassCastException e){ return null;}
    }


    private <T extends IRegExGenerator<?>> SettingsMap<T> castSingleSettingsContainer(SettingsMap<?> singleSettingsContainer, Class<T> clazz){
        SettingsMap<T> newContainer = new SettingsMap<>();

        for (Map.Entry<SettingsOption,?> entry : singleSettingsContainer.entrySet()){
            try{
                newContainer.put(entry.getKey(), clazz.cast(entry.getValue()));
            }catch (ClassCastException e){
                //continue trying to cast other values
            }
        }
        return newContainer;
    }

    public Map<SettingsOption, IRegExGenerator<?>> getAllSettings(){
        return this.allSettings;
    }

}
