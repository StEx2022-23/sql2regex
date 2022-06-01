package sqltoregex.settings;

import sqltoregex.settings.regexgenerator.IRegExGenerator;

import java.util.EnumMap;
import java.util.Map;

public class SettingsMap<V extends IRegExGenerator<?>> extends EnumMap<SettingsOption,V> {

    SettingsMap(){
        super(SettingsOption.class);
    }

    public V get(SettingsOption settingsOption){
        return getOrDefault(settingsOption, null);
    }

    @Override
    public V get(Object key) {
        if(!(key instanceof SettingsOption)){
            throw new UnsupportedOperationException("Not allowed for this implementation.");
        }
        return super.get(key);
    }

    @Override
    public void putAll(Map<? extends SettingsOption, ? extends V> m) {
        super.putAll(m);
    }
}