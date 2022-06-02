package sqltoregex.settings;

import sqltoregex.settings.regexgenerator.IRegExGenerator;

import java.util.Map;

public class SettingsContainer {
    SettingsMap<IRegExGenerator<?>> allSettings = new SettingsMap<>();

    public SettingsContainer with(IRegExGenerator<?> regExGenerator){
        this.allSettings.put(regExGenerator.getSettingsOption(), regExGenerator);
        return this;
    }

    public void putAll(SettingsContainer settingsContainer) {
        allSettings.putAll(settingsContainer.allSettings);
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

    public <T extends IRegExGenerator<?>> SettingsMap<T> get(Class<T> clazz){
        SettingsMap<T> newContainer = new SettingsMap<>();

        for (Map.Entry<SettingsOption,?> entry : this.allSettings.entrySet()){
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
