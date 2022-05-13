package sqltoregex.property;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;
import org.xml.sax.SAXException;
import sqltoregex.property.PropertyManager;
import sqltoregex.property.PropertyOption;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PropertyManagerTest {
    PropertyManager propertyManager = new PropertyManager();

    PropertyManagerTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
    }


    @Test
    @Order(1)
    void testLoadDefaultProperties() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        PropertyManager propertyManager = new PropertyManager();
        Set<PropertyOption> propertyOptionSet = propertyManager.readPropertyOptions();
        List<String> propertyOptionWhichHaveBeenSet = List.of("KEYWORDSPELLING", "TABLENAMESPELLING", "TABLENAMEORDER","COLUMNNAMESPELLING","KEYWORDSPELLING","COLUMNNAMEORDER","DATESYNONYMS", "TIMESYNONYMS", "DATETIMESYNONYMS", "SUMSYNONYM", "AVGSYNONYM");
        System.out.println(propertyOptionSet);
        for(String str : propertyOptionWhichHaveBeenSet){
            Assertions.assertTrue(propertyOptionSet.toString().contains(str));
        }
    }

    @Test
    @Order(2)
    void testUserConfigurations() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Set<PropertyOption> spellings = new HashSet<>();
        spellings.add(PropertyOption.KEYWORDSPELLING);
        Set<PropertyOption> orders = new HashSet<>();
        orders.add(PropertyOption.COLUMNNAMEORDER);
        Set<SimpleDateFormat> dateFormats = new HashSet<>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        Set<SimpleDateFormat> timeFormats = new HashSet<>();
        Set<SimpleDateFormat> dateTimeFormats = new HashSet<>();
        Pair<String, String> sumSynonyms = new MutablePair<>();
        Pair<String, String> avgSynonyms = new MutablePair<>();
        String sql = "Select * From test";
        PropertyForm propertyForm = new PropertyForm(
                spellings, orders, dateFormats, timeFormats, dateTimeFormats, sumSynonyms, avgSynonyms, sql
        );

        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        propertyMapBuilder.with(PropertyOption.KEYWORDSPELLING);
        propertyMapBuilder.with(PropertyOption.COLUMNNAMEORDER);

        Set<String> dateSynonymsSet = new HashSet<>();
        dateSynonymsSet.add("yyyy-MM-dd");
        propertyMapBuilder.with(dateSynonymsSet, PropertyOption.DATESYNONYMS);

        Assertions.assertEquals(propertyMapBuilder.build(), propertyManager.parseUserOptionsInput(propertyForm));
    }

    @Test
    @Order(3)
    void testUserConfigurationsEverythingDisabeld() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Set<PropertyOption> spellings = new HashSet<>();
        Set<PropertyOption> orders = new HashSet<>();
        Set<SimpleDateFormat> dateFormats = new HashSet<>();
        Set<SimpleDateFormat> timeFormats = new HashSet<>();
        Set<SimpleDateFormat> dateTimeFormats = new HashSet<>();
        Pair<String, String> sumSynonyms = new MutablePair<>();
        Pair<String, String> avgSynonyms = new MutablePair<>();
        String sql = "Select * From test";
        PropertyForm propertyForm = new PropertyForm(spellings, orders, dateFormats, timeFormats, dateTimeFormats, sumSynonyms, avgSynonyms, sql);
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        Assertions.assertEquals(propertyMapBuilder.build(), propertyManager.parseUserOptionsInput(propertyForm));
    }
}
