package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PropertyManagerTest {
    static PropertyManager propertyManager;
    static Set<PropertyOption> spellings;
    static Set<PropertyOption> orders;
    static Set<SimpleDateFormat> dateFormats;
    static Set<SimpleDateFormat> timeFormats;
    static Set<SimpleDateFormat> dateTimeFormats;
    static Set<String> aggregateFunctionLang;
    static String sql;

    @BeforeEach
    void beforeEach() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        this.resetSets();
    }

    void resetSets() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        propertyManager = new PropertyManager();
        spellings = new HashSet<>();
        orders = new HashSet<>();
        dateFormats = new HashSet<>();
        timeFormats = new HashSet<>();
        dateTimeFormats = new HashSet<>();
        aggregateFunctionLang = new HashSet<>();
        sql = "SELECT * FROM test";
    }

    @Test
    void testLoadDefaultProperties() {
        Set<PropertyOption> propertyOptionSet = propertyManager.readPropertyOptions();
        List<String> propertyOptionWhichHaveBeenSet = List.of(
                "KEYWORDSPELLING",
                "TABLENAMESPELLING",
                "TABLENAMEORDER",
                "COLUMNNAMESPELLING",
                "KEYWORDSPELLING",
                "COLUMNNAMEORDER",
                "DATESYNONYMS",
                "TIMESYNONYMS",
                "DATETIMESYNONYMS",
                "AGGREGATEFUNCTIONLANG"
        );
        for (String str : propertyOptionWhichHaveBeenSet) {
            Assertions.assertTrue(propertyOptionSet.toString().contains(str));
        }
    }

    @Test
    void testGetPropertyByClazz(){
        Assertions.assertEquals(3, propertyManager.getPropertyByClass(SpellingMistake.class).size());
        Assertions.assertEquals(2, propertyManager.getPropertyByClass(OrderRotation.class).size());
        Assertions.assertEquals(1, propertyManager.getPropertyByClass(StringSynonymGenerator.class).size());
    }

    @Test
    void testGetProperty(){
        for (PropertyOption propertyOption : PropertyOption.values()) {
            if(PropertyOption.DEFAULT.equals(propertyOption)){
                continue;
            }
            Assertions.assertTrue(propertyManager.getPropertyMap().containsKey(propertyOption));
            Assertions.assertTrue(propertyManager.getPropertyMap().get(propertyOption) != null);
        }
    }
}
