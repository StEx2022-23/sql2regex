package sqltoregex.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PropertyMapBuilder {
    //Tipp for later use EnumMap for initialisation, more efficient and otherwise a codesmell
    private final Map<PropertyOption, Property> propertyMap;

    @Autowired
    public PropertyMapBuilder(PropertyManager propertyManager){
        this.propertyMap = propertyManager.getPropertyMap();
    }

    public Map<String, Property> intersection(Map<PropertyOption, List<String>> userDefinitions){
        return Collections.emptyMap();
    }

    public Map<String, Property> substract(){
        return Collections.emptyMap();
    }

    public void deleteKey(PropertyOption key){
        this.propertyMap.remove(key);
    }

    public void deleteSynonym(DateAndTimeFormatSynonymGenerator o, SimpleDateFormat sdf){
        o.removeSynonym(sdf);
    }
}
