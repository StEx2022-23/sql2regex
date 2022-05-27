package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.deparser.UserSettingsTestCase;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class RegExGeneratorTest extends UserSettingsTestCase {

    public RegExGeneratorTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        super(SettingsType.USER);
    }

    @Test
    void testUseSpellingMistake(){
        String keyword = "SELECT";
        List<String> keywordOptions = List.of("SELECT", "ELECT", "SLECT", "SELET", "SELEC");
        String generatedRegEx = RegExGenerator.useSpellingMistake(new SpellingMistake(SettingsOption.KEYWORDSPELLING), keyword);
        for(String str : keywordOptions){
            Assertions.assertTrue(generatedRegEx.contains(str));
        }

        String generatedRegExWithoutSpellingMistake = RegExGenerator.useSpellingMistake(null, keyword);
        Assertions.assertTrue(generatedRegExWithoutSpellingMistake.equals("SELECT"));
    }

    @Test
    void testUseStringSynonymGenerator(){
        StringSynonymGenerator stringSynonymGenerator = new StringSynonymGenerator(SettingsOption.DEFAULT);
        stringSynonymGenerator.addSynonym("Cola");
        stringSynonymGenerator.addSynonym("Coke");
        String regex = RegExGenerator.useStringSynonymGenerator(stringSynonymGenerator, "Cola");

        Assertions.assertTrue(regex.contains("Cola"));
        Assertions.assertTrue(regex.contains("Coke"));

        Assertions.assertTrue(RegExGenerator.useStringSynonymGenerator(null, "Cola").equals("Cola"));
    }

    @Test
    void testUseExpressionSynonymGenerator(){
        DateAndTimeFormatSynonymGenerator expressionSynonymGenerator = new DateAndTimeFormatSynonymGenerator(SettingsOption.DEFAULT);
        expressionSynonymGenerator.addSynonym(new SimpleDateFormat("yyyy-MM-dd"));
        expressionSynonymGenerator.addSynonym(new SimpleDateFormat("yyyy-M-d"));
        String regex = RegExGenerator.useExpressionSynonymGenerator(expressionSynonymGenerator, new DateValue("'2022-05-03'"));
        Assertions.assertTrue(regex.contains("2022-05-03"));
        Assertions.assertTrue(regex.contains("2022-5-3"));

        regex = RegExGenerator.useExpressionSynonymGenerator(null, new DateValue("'2022-05-03'"));
        Assertions.assertEquals("2022-05-03", regex);
    }

    @Test
    void testUseOrderRotation(){
        List<String> stringList = new LinkedList<>();
        stringList.add("1");
        stringList.add("2");
        String orderRotatedExpections = "(?:1\\s*,\\s*2|2\\s*,\\s*1)";
        String orderRotatedList = RegExGenerator.useOrderRotation(new OrderRotation(SettingsOption.TABLENAMEORDER), stringList);
        Assertions.assertEquals(orderRotatedExpections, orderRotatedList);

        List<String> nonRotatedList = new LinkedList<>();
        nonRotatedList.add("1");
        nonRotatedList.add("2");
        String nonOrderRotatedExpectation = "1\\s*,\\s*2";
        String regex = RegExGenerator.useOrderRotation(null, nonRotatedList);
        Assertions.assertEquals(nonOrderRotatedExpectation, regex);
    }

    @Test
    void useExpressionRotation() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        ExpressionRotation expressionRotation = new ExpressionRotation(SettingsOption.DEFAULT);

        List<Expression> expressionList = new LinkedList<>(List.of(new Column("a"), new Column("b")));
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SettingsManager());
        expressionDeParserForRegEx.setBuffer(buffer);
        String regex = RegExGenerator.useExpressionRotation(expressionRotation, expressionDeParserForRegEx, expressionList);
        Assertions.assertNotEquals("a\\s*,\\s*b", regex);

        List<Expression> expressionListWithOutRotation = new LinkedList<>(List.of(new Column("a"), new Column("b")));


        ExpressionDeParserForRegEx expressionDeParserForRegExWithoutRotation = new ExpressionDeParserForRegEx(new SettingsManager());
        String regexWithoutRotation = RegExGenerator.useExpressionRotation(
                null,
                expressionDeParserForRegExWithoutRotation,
                expressionListWithOutRotation
        );
        Assertions.assertEquals("\\s*a\\s*,\\s*b\\s*", expressionDeParserForRegExWithoutRotation.getBuffer().toString());
    }
}
