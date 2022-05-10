package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.PropertyManager;
import sqltoregex.property.PropertyOption;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

class PropertyManagerTest {
    @Test
    void testLoadDefaultProperties() throws ParserConfigurationException, IOException, ClassNotFoundException, InvocationTargetException, SAXException, NoSuchMethodException, InstantiationException, IllegalAccessException, XPathExpressionException {
        PropertyManager propertyManager = new PropertyManager();
        Set<PropertyOption> propertyOptionSet = propertyManager.readPropertyOptions();
        List<String> propertyOptionWhichHaveBeenSet = List.of("TABLENAMEORDER","KEYWORDSPELLING","COLUMNNAMEORDER","DATESYNONYMS");
        for(String str : propertyOptionWhichHaveBeenSet){
            Assertions.assertTrue(propertyOptionSet.toString().contains(str));
        }
    }
}
