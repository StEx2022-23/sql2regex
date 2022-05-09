package sqltoregex.PropertyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.PropertyManager;
import sqltoregex.property.PropertyOptions;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

class PropertyManagerTest {
    @Test
    void testLoadDefaultProperties() throws ParserConfigurationException, IOException, ClassNotFoundException, InvocationTargetException, SAXException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        PropertyManager propertyManager = new PropertyManager();
        propertyManager.parseProperties();
        Set<PropertyOptions> propertyOptionSet = propertyManager.readPropertyOptions();
        List<String> propertyOptionWhichHaveBeenSet = List.of("tablenameorder","keywordspelling","columnnameorder","datesynonyms");
        for(String str : propertyOptionWhichHaveBeenSet){
            Assertions.assertTrue(propertyOptionSet.toString().contains(str));
        }
    }
}
