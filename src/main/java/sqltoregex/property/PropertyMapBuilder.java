package sqltoregex.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Component
public class PropertyMapBuilder {
    private final Map<PropertyOptions, Property> propertyMap;

    @Autowired
    public PropertyMapBuilder(PropertyManager propertyManager){
        this.propertyMap = propertyManager.getPropertyMap();
    }

    public Map<String, Property> intersection(Map<PropertyOptions, List<String>> userDefinitions){
        return null;
    }

    public Map<String, Property> substract(){
        return null;
    }

    public void deleteKey(PropertyOptions key){
        this.propertyMap.remove(key);
    }

    public void deleteSynonym(DateSynonymManager o, SimpleDateFormat sdf){
        o.removeSynonym(sdf);
    }
}
