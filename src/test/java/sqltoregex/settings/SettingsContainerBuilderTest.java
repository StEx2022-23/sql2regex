package sqltoregex.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

class SettingsContainerBuilderTest {

    SettingsContainer.Builder builder;

    @BeforeEach
    void beforeEach() {
        builder = SettingsContainer.builder();
    }

    @Test
    void buildWith2EqualObjects() {
        builder.with(SettingsOption.COLUMNNAMEORDER);
        builder.with(SettingsOption.COLUMNNAMEORDER);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
    }

    @Test
    void buildWithColumnNameOrder() {
        builder.with(SettingsOption.COLUMNNAMEORDER);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.COLUMNNAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(SettingsOption.COLUMNNAMEORDER)));
    }

    @Test
    void buildWithDateSynonyms() {
        final String FORMAT = "yyyy-MM-dd";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        SettingsOption.DATESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DATESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsKey(SettingsOption.DATESYNONYMS));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void buildWithDateTimeSynonyms() {
        final String FORMAT = "yyyy-MM-dd hh:mm:ss";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        SettingsOption.DATETIMESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.DATETIMESYNONYMS));
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DATETIMESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void buildWithEmptyAggregateFunctionLang() {
        builder.withStringSet(Collections.emptySet(), SettingsOption.AGGREGATEFUNCTIONLANG);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void buildWithEmptyDateSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), SettingsOption.DATESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.DATESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(SettingsOption.DATESYNONYMS)));
    }

    @Test
    void buildWithEmptyDateTimeSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), SettingsOption.DATETIMESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.DATETIMESYNONYMS));
        Assertions.assertTrue(
                map.containsValue(new DateAndTimeFormatSynonymGenerator(SettingsOption.DATETIMESYNONYMS)));
    }

    @Test
    void buildWithEmptySetOfPropertyOption() {
        builder.withSettingsOptionSet(new HashSet<>());
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(0, map.size());
    }

    @Test
    void buildWithEmptyTimeSynonyms() {
        builder.withSimpleDateFormatSet(Collections.emptySet(), SettingsOption.TIMESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.TIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(new DateAndTimeFormatSynonymGenerator(SettingsOption.TIMESYNONYMS)));
    }

    @Test
    void buildWithKeyWordSpelling() {
        builder.with(SettingsOption.KEYWORDSPELLING);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.KEYWORDSPELLING));
        Assertions.assertTrue(map.containsValue(new SpellingMistake(SettingsOption.KEYWORDSPELLING)));
    }

    @Test
    void buildWithTableNameOrder() {
        builder.with(SettingsOption.TABLENAMEORDER);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.TABLENAMEORDER));
        Assertions.assertTrue(map.containsValue(new OrderRotation(SettingsOption.TABLENAMEORDER)));
    }

    @Test
    void buildWithTimeSynonyms() {
        final String FORMAT = "hh:mm:ss";
        builder.withSimpleDateFormatSet(new HashSet<>(List.of(new SimpleDateFormat(FORMAT))),
                                        SettingsOption.TIMESYNONYMS);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        DateAndTimeFormatSynonymGenerator dateAndTimeFormatSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.TIMESYNONYMS);
        dateAndTimeFormatSynonymGenerator.addSynonym(new SimpleDateFormat(FORMAT));
        Assertions.assertTrue(map.containsKey(SettingsOption.TIMESYNONYMS));
        Assertions.assertTrue(map.containsValue(dateAndTimeFormatSynonymGenerator));
    }

    @Test
    void builderStartsWithEmptyMap() {
        Assertions.assertEquals(Collections.emptyMap(), builder.build().getAllSettings());
    }

    @Test
    void testAggregateFunctionLangInSettingsManager() {
        String PAIR_ONE = "SUMME;SUM";
        String PAIR_TWO = "MITTELWERT;AVG";
        Set<String> aggregateFunctionLang = new HashSet<>();
        aggregateFunctionLang.add(PAIR_ONE);
        aggregateFunctionLang.add(PAIR_TWO);
        builder.withStringSet(aggregateFunctionLang, SettingsOption.AGGREGATEFUNCTIONLANG);
        Map<SettingsOption, IRegExGenerator<?>> map = builder.build().getAllSettings();
        Assertions.assertEquals(1, map.size());
        Assertions.assertTrue(map.containsKey(SettingsOption.AGGREGATEFUNCTIONLANG));
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(
                SettingsOption.AGGREGATEFUNCTIONLANG);
        stringSynonymGenerator.addSynonymFor(PAIR_ONE.split(";")[0], PAIR_ONE.split(";")[1]);
        stringSynonymGenerator.addSynonymFor(PAIR_TWO.split(";")[0], PAIR_TWO.split(";")[1]);
        Assertions.assertTrue(map.containsValue(stringSynonymGenerator));
    }
}
