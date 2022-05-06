package sqltoregex.property;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

public class DateAndTimeSynonymGeneratorTest {


    private DateAndTimeSynonymGenerator getSynonymManagerForDates(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yy-MM-dd"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeSynonymGenerator getSynonymManagerForTimes(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("H:m:s"));
        return dateAndTimeSynonymManager;
    }

    private DateAndTimeSynonymGenerator getSynonymManagerForTimestamps(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeSynonymGenerator();
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        dateAndTimeSynonymManager.addSynonym(new SimpleDateFormat("yyyy-MM-dd H:m:s"));
        return dateAndTimeSynonymManager;
    }

    @Test
    public void queryNotExistingSynonym(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = new DateAndTimeSynonymGenerator();
        Assertions.assertEquals("2012-05-06", dateAndTimeSynonymManager.generateSynonymRegexFor(new DateValue("'2012-5-6'")));
    }

    @Test
    public void queryExistingDateSynonym(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForDates();
        Assertions.assertEquals("2012-05-06|12-05-06", dateAndTimeSynonymManager.generateSynonymRegexFor(new DateValue("'2012-05-06'")));
    }

    @Test
    public void queryExistingTimeSynonym(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimes();
        Assertions.assertEquals("01:02:03|1:2:3", dateAndTimeSynonymManager.generateSynonymRegexFor(new TimeValue("'01:02:03'")));
    }

    @Test
    public void queryExistingTimestampSynonym(){
        DateAndTimeSynonymGenerator dateAndTimeSynonymManager = getSynonymManagerForTimestamps();
        Assertions.assertEquals("2012-05-06 01:02:03|2012-05-06 1:2:3", dateAndTimeSynonymManager.generateSynonymRegexFor(new TimestampValue("2012-05-06 01:02:03")));
    }
}
