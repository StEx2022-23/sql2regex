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

    public SettingsContainer with(SettingsManager settingsManager, SettingsType settingsType){
        for (IRegExGenerator<?> generator : settingsManager.getSettingsMap(settingsType).values()){
            this.with(generator);
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
