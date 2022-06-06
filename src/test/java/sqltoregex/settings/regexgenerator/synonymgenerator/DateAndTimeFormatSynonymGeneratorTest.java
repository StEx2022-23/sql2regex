package sqltoregex.settings.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsOption;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

class DateAndTimeFormatSynonymGeneratorTest {


    private DateAndTimeFormatSynonymGenerator getSynonymManagerForDates() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yy-MM-dd"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimes() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("H:m:s"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimestamps() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd H:m:s"));
        return dateAndTimeSynonymManager;
    }

    @Test
    void queryExistingDateSynonym() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForDates();
        Assertions.assertEquals("(?:2012-05-06|12-05-06)",
                                dateAndTimeSynonymManager.generateRegExFor(new DateValue("'2012-05-06'")));
    }

    @Test
    void queryExistingTimeSynonym() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimes();
        Assertions.assertEquals("(?:01:02:03|1:2:3)",
                                dateAndTimeSynonymManager.generateRegExFor(new TimeValue("'01:02:03'")));
    }

    @Test
    void queryExistingTimestampSynonym() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimestamps();
        Assertions.assertEquals("(?:2012-05-06 01:02:03|2012-05-06 1:2:3)",
                                dateAndTimeSynonymManager.generateRegExFor(new TimestampValue("2012-05-06 01:02:03")));
    }

    @Test
    void queryNotExistingSynonym() {
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DEFAULT);
        Assertions.assertEquals("(?:2012-5-6)",
                                dateAndTimeSynonymManager.generateRegExFor(new DateValue("'2012-5-6'")));
        Assertions.assertEquals("(?:2012-05-06)",
                                dateAndTimeSynonymManager.generateRegExFor(new DateValue("'2012-05-06'")));
    }

    @Test
    void testGenerateAsList(){
        final DateValue dateValue = new DateValue("'2022-06-30'");
        Assertions.assertEquals(new LinkedList<>(List.of("2022-06-30")), DateAndTimeFormatSynonymGenerator.generateAsListOrDefault(null, dateValue));

        DateAndTimeFormatSynonymGenerator generator = new DateAndTimeFormatSynonymGenerator(SettingsOption.DEFAULT);
        generator.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        generator.addSynonym(new SimpleDateFormat("yyyy-M-d"));

        List<String> synonyms = List.of("2022-06-30",
                                        "2022-6-30");

        List<String> generatedSynonyms = DateAndTimeFormatSynonymGenerator.generateAsListOrDefault(generator, dateValue);
        Assertions.assertEquals(synonyms.size(), generatedSynonyms.size());
        for (String synonym : generatedSynonyms){
            Assertions.assertTrue(synonyms.contains(synonym));
        }
    }

    @Test
    void testUseOrDefault() {
        DateAndTimeFormatSynonymGenerator expressionSynonymGenerator = new DateAndTimeFormatSynonymGenerator(
                SettingsOption.DEFAULT);
        expressionSynonymGenerator.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        expressionSynonymGenerator.addSynonym(new SimpleDateFormat("yyyy-M-d"));
        String regex = DateAndTimeFormatSynonymGenerator.useOrDefault(expressionSynonymGenerator,
                                                                      new DateValue("'2022-05-03'"));
        Assertions.assertTrue(regex.contains("2022-05-03"));
        Assertions.assertTrue(regex.contains("2022-5-3"));

        regex = DateAndTimeFormatSynonymGenerator.useOrDefault(null, new DateValue("'2022-05-03'"));
        Assertions.assertEquals("2022-05-03", regex);
    }
}
