package sqltoregex.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

class PropertyMapBuilderTest {

    PropertyMapBuilder builder;

    @Test
    void OrderAndSpellingWithSameProp() {
        builder.withPropertyOption(PropertyOption.COLUMNNAMEORDER);
        builder.withPropertyOption(PropertyOption.COLUMNNAMESPELLING);
        OrderRotation orderRotation = (OrderRotation) builder.build().get(PropertyOption.COLUMNNAMEORDER);
        Assertions.assertEquals(new SpellingMistake(PropertyOption.COLUMNNAMESPELLING),
                                orderRotation.getSpellingMistake());
    }

    @BeforeEach
    void beforeEach() {
        builder = new PropertyMapBuilder();
    }

    @Test
    void buildWith2EqualObjects() {
        builder.withPropertyOption(PropertyOption.COLUMNNAMEORDER);
        builder.withPropertyOption(PropertyOption.COLUMNNAMEORDER);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
    }

    @Test
    void buildWithColumnNameOrder() {
        builder.withPropertyOption(PropertyOption.COLUMNNAMEORDER);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.COLUMNNAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(PropertyOption.COLUMNNAMEORDER)));
    }

    @Test
    void buildWithDateSynonyms() {
        final String FORMAT = "yyyy-MM-dd";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        PropertyOption.DATESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                PropertyOption.DATESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsKey(PropertyOption.DATESYNONYMS));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void buildWithDateTimeSynonyms() {
        final String FORMAT = "yyyy-MM-dd hh:mm:ss";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        PropertyOption.DATETIMESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.DATETIMESYNONYMS));
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                PropertyOption.DATETIMESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void buildWithEmptyAggregateFunctionLang() {
        builder.withStringSet(Collections.emptySet(), PropertyOption.AGGREGATEFUNCTIONLANG);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void buildWithEmptyDateSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), PropertyOption.DATESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.DATESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.DATESYNONYMS)));
    }

    @Test
    void buildWithEmptyDateTimeSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), PropertyOption.DATETIMESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.DATETIMESYNONYMS));
        Assertions.assertTrue(
                map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.DATETIMESYNONYMS)));
    }

    @Test
    void buildWithEmptySetOfPropertyOption() {
        builder.withPropertyOptionSet(new HashSet<>());
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void buildWithEmptyTimeSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), PropertyOption.TIMESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.TIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.TIMESYNONYMS)));
    }

    @Test
    void buildWithKeyWordSpelling() {
        builder.withPropertyOption(PropertyOption.KEYWORDSPELLING);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.KEYWORDSPELLING));
        Assertions.assertTrue(map.containsValue(new SpellingMistake(PropertyOption.KEYWORDSPELLING)));
    }

    @Test
    void buildWithTableNameOrder() {
        builder.withPropertyOption(PropertyOption.TABLENAMEORDER);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.TABLENAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(PropertyOption.TABLENAMEORDER)));
    }

    @Test
    void buildWithTimeSynonyms() {
        final String FORMAT = "hh:mm:ss";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        PropertyOption.TIMESYNONYMS);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                PropertyOption.TIMESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsKey(PropertyOption.TIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void builderStartsWithEmptyMap() {
        Assertions.assertEquals(Collections.emptyMap(), builder.build());
    }

    @Test
    void testAggregateFunctionLangInPropertyManager() {
        String PAIR_ONE = "SUMME;SUM";
        String PAIR_TWO = "MITTELWERT;AVG";
        Set<String> aggregateFunctionLang = new HashSet<String>();
        aggregateFunctionLang.add(PAIR_ONE);
        aggregateFunctionLang.add(PAIR_TWO);
        builder.withStringSet(aggregateFunctionLang, PropertyOption.AGGREGATEFUNCTIONLANG);
        Map<PropertyOption, RegExGenerator<?, ?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.AGGREGATEFUNCTIONLANG));
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(
                PropertyOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor(PAIR_ONE.split(";")[0], PAIR_ONE.split(";")[1]);
        stringSynonymGenerator.addSynonymFor(PAIR_TWO.split(";")[0], PAIR_TWO.split(";")[1]);
        Assertions.assertTrue(map.containsValue(stringSynonymGenerator));
    }
}
