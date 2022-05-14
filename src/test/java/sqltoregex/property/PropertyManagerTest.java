package sqltoregex.property;

import org.junit.jupiter.api.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

class PropertyManagerTest {
    static PropertyManager propertyManager;
    static Set<PropertyOption> spellings;
    static Set<PropertyOption> orders;
    static Set<SimpleDateFormat> dateFormats;
    static Set<SimpleDateFormat> timeFormats;
    static Set<SimpleDateFormat> dateTimeFormats;
    static Set<String> aggregateFunctionLang;
    static String sql;

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
    void testLoadDefaultProperties() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        this.resetSets();
        PropertyManager propertyManager = new PropertyManager();
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
        System.out.println(propertyOptionSet);
        for(String str : propertyOptionWhichHaveBeenSet){
            Assertions.assertTrue(propertyOptionSet.toString().contains(str));
        }
    }

    @Test
    void testUserConfigurations() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        resetSets();
        spellings.add(PropertyOption.KEYWORDSPELLING);
        orders.add(PropertyOption.COLUMNNAMEORDER);
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        aggregateFunctionLang.add("SUM,SUMME");
        PropertyForm propertyForm = new PropertyForm(
                spellings, orders, dateFormats, timeFormats, dateTimeFormats, aggregateFunctionLang, sql
        );

        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        propertyMapBuilder.with(PropertyOption.KEYWORDSPELLING);
        propertyMapBuilder.with(PropertyOption.COLUMNNAMEORDER);

        Set<String> dateSynonymsSet = new HashSet<>();
        dateSynonymsSet.add("yyyy-MM-dd");
        propertyMapBuilder.with(dateSynonymsSet, PropertyOption.DATESYNONYMS);
        propertyMapBuilder.with(aggregateFunctionLang, PropertyOption.AGGREGATEFUNCTIONLANG);

        Assertions.assertEquals(propertyMapBuilder.build(), propertyManager.parseUserOptionsInput(propertyForm));
    }

    @Test
    void testUserConfigurationsWhenEverythingIsDisabeld() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        resetSets();
        PropertyForm propertyForm = new PropertyForm(spellings, orders, dateFormats, timeFormats, dateTimeFormats, aggregateFunctionLang, sql);
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        Assertions.assertEquals(propertyMapBuilder.build(), propertyManager.parseUserOptionsInput(propertyForm));
    }

    @Test
    void testAggregateFunctionLangInPropertyManager() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        resetSets();
        aggregateFunctionLang.add("SUMME,SUM");
        aggregateFunctionLang.add("MITTELWERT,AVG");
        PropertyForm propertyForm = new PropertyForm(spellings, orders, dateFormats, timeFormats, dateTimeFormats, aggregateFunctionLang, sql);
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        propertyMapBuilder.with(aggregateFunctionLang, PropertyOption.AGGREGATEFUNCTIONLANG).build();
        Assertions.assertEquals(propertyMapBuilder.build(), propertyManager.parseUserOptionsInput(propertyForm));

        Set<?> getSettingsSet = propertyManager.parseUserOptionsInput(propertyForm).get(PropertyOption.AGGREGATEFUNCTIONLANG).getSettings();
        Assertions.assertEquals(2, getSettingsSet.size());
        Assertions.assertTrue(getSettingsSet.toString().contains("SUMME"));
        Assertions.assertTrue(getSettingsSet.toString().contains("SUM"));
        Assertions.assertTrue(getSettingsSet.toString().contains("AVG"));
        Assertions.assertTrue(getSettingsSet.toString().contains("MITTELWERT"));
    }
}
