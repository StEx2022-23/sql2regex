package sqltoregex.property;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

public class DateAndTimeFormatSynonymGeneratorTest {


    private DateAndTimeFormatSynonymGenerator getSynonymManagerForDates(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yy-MM-dd"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimes(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("H:m:s"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeFormatSynonymGenerator getSynonymManagerForTimestamps(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd H:m:s"));
        return dateAndTimeSynonymManager;
    }

    @Test
    public void queryNotExistingSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeFormatSynonymGenerator();
        Assertions.assertEquals("2012-05-06", dateAndTimeSynonymManager.generateSynonymRegexFor(new DateValue("'2012-5-6'")));
    }

    @Test
    public void queryExistingDateSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForDates();
        Assertions.assertEquals("2012-05-06|12-05-06", dateAndTimeSynonymManager.generateSynonymRegexFor(new DateValue("'2012-05-06'")));
    }

    @Test
    public void queryExistingTimeSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimes();
        Assertions.assertEquals("01:02:03|1:2:3", dateAndTimeSynonymManager.generateSynonymRegexFor(new TimeValue("'01:02:03'")));
    }

    @Test
    public void queryExistingTimestampSynonym(){
        DateAndTimeFormatSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimestamps();
        Assertions.assertEquals("2012-05-06 01:02:03|2012-05-06 1:2:3", dateAndTimeSynonymManager.generateSynonymRegexFor(new TimestampValue("2012-05-06 01:02:03")));
    }
}
