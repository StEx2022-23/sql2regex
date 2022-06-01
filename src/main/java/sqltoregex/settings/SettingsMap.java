package sqltoregex.settings;

import sqltoregex.settings.regexgenerator.IRegExGenerator;

import java.util.EnumMap;

public class SettingsMap<V extends IRegExGenerator<?>> extends EnumMap<SettingsOption,V> {
    public static final String NOT_ALLOWED_FOR_THIS_IMPLEMENTATION = "Not allowed for this implementation.";

    SettingsMap(){
        super(SettingsOption.class);
    }

    public V get(SettingsOption settingsOption){
        return getOrDefault(settingsOption, null);
    }

    @Override
    public V get(Object key) {
        if(!(key instanceof SettingsOption)){
            throw new UnsupportedOperationException(NOT_ALLOWED_FOR_THIS_IMPLEMENTATION);
        }
        return super.get(key);
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException(NOT_ALLOWED_FOR_THIS_IMPLEMENTATION);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(NOT_ALLOWED_FOR_THIS_IMPLEMENTATION);
    }
}