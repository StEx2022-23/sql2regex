package sqltoregex.property.regexgenerator.synonymgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.property.PropertyOption;

import java.text.SimpleDateFormat;

class DateAndTimeFormatSynonymGeneratorTest {


    private DateAndTimeFormatSynonymGenerator getSynonymManagerForDates(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(
                PropertyOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yy-MM-dd"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimes(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(PropertyOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("H:m:s"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimestamps(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(PropertyOption.DEFAULT);
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd H:m:s"));
        return dateAndTimeSynonymManager;
    }

    @Test
    void queryNotExistingSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator(PropertyOption.DEFAULT);
        Assertions.assertEquals("2012-05-06", dateAndTimeSynonymManager.generateRegExFor(new DateValue("'2012-5-6'")));
    }

    @Test
    void queryExistingDateSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForDates();
        Assertions.assertEquals("2012-05-06|12-05-06", dateAndTimeSynonymManager.generateRegExFor(new DateValue("'2012-05-06'")));
    }

    @Test
    void queryExistingTimeSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimes();
        Assertions.assertEquals("01:02:03|1:2:3", dateAndTimeSynonymManager.generateRegExFor(new TimeValue("'01:02:03'")));
    }

    @Test
    void queryExistingTimestampSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimestamps();
        Assertions.assertEquals("2012-05-06 01:02:03|2012-05-06 1:2:3", dateAndTimeSynonymManager.generateRegExFor(new TimestampValue("2012-05-06 01:02:03")));
    }
}
