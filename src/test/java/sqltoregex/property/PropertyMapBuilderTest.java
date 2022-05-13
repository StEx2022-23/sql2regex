package sqltoregex.property;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.Collections;
import java.util.Map;

public class PropertyMapBuilderTest {

    PropertyMapBuilder builder;

    @BeforeEach
    void beforeEach(){
        builder = new PropertyMapBuilder();
    }

    @Test
    void builderStartsWithEmptyMap(){
        Assertions.assertEquals(builder.build(), Collections.emptyMap());
    }

    @Test
    void buildWithKeyWordSpelling(){
        builder.with(PropertyOption.KEYWORDSPELLING);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.KEYWORDSPELLING));
        Assertions.assertTrue(map.containsValue(new SpellingMistake(PropertyOption.KEYWORDSPELLING)));
    }

    @Test
    void buildWithTableNameOrder(){
        builder.with(PropertyOption.TABLENAMEORDER);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.TABLENAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(PropertyOption.TABLENAMEORDER)));
    }

    @Test
    void buildWithColumnNameOrder(){
        builder.with(PropertyOption.COLUMNNAMEORDER);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.COLUMNNAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(PropertyOption.COLUMNNAMEORDER)));
    }

    @Test
    void buildWithDateSynonyms(){
        builder.with(Collections.emptySet(), PropertyOption.DATESYNONYMS);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.DATESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.DATESYNONYMS)));
    }

    @Test
    void buildWithTimeSynonyms(){
        builder.with(Collections.emptySet(), PropertyOption.TIMESYNONYMS);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.TIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.TIMESYNONYMS)));
    }

    @Test
    void buildWithDateTimeSynonyms(){
        builder.with(Collections.emptySet(), PropertyOption.DATETIMESYNONYMS);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(PropertyOption.DATETIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(PropertyOption.DATETIMESYNONYMS)));
    }

    @Test
    void buildWith2EqualObjects(){
        builder.with(PropertyOption.COLUMNNAMEORDER);
        builder.with(PropertyOption.COLUMNNAMEORDER);
        Map<PropertyOption, Property<?>> map = builder.build();
        Assertions.assertEquals(1, map.size());
    }

    @Test
    void OrderAndSpellingWithSameProp(){
        builder.with(PropertyOption.COLUMNNAMEORDER);
        builder.with(PropertyOption.COLUMNNAMESPELLING);
        OrderRotation orderRotation = (OrderRotation) builder.build().get(PropertyOption.COLUMNNAMEORDER);
        Assertions.assertEquals(new SpellingMistake(PropertyOption.COLUMNNAMESPELLING), orderRotation.getSpellingMistake());
    }
}
