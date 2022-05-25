package sqltoregex.equivalentStatements;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;
import sqltoregex.ConverterManagement;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EquivalentStatementTest {
    Map<String, List<String>> equivalentStatements;
    private static final String DELIMITER_FOR_EQUIVALENTS = "#####";
    private static final String DELIMITER_FOR_SINGLE_STATEMENTS = ";";

    ConverterManagement converterManagement;

    public EquivalentStatementTest() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        converterManagement = new ConverterManagement(new SettingsManager());
    }

    void parseTextFile(SupportedStatementType statementType) throws IOException {
        equivalentStatements = new HashMap<>();
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/sqltoregex/equivalentStatements/"+statementType)));
        data = data.replaceAll("<!--.*--!>", "");
        data = data.replace("\r", "").replace("\n", "");
        String[] splitByEquivalents = data.split(DELIMITER_FOR_EQUIVALENTS);
        for(String str : splitByEquivalents){
            String[] splitBySingleStatements = str.split(DELIMITER_FOR_SINGLE_STATEMENTS);
            List<String> equivalents = new ArrayList<>(Arrays.asList(splitBySingleStatements).subList(1, splitBySingleStatements.length));
            this.equivalentStatements.put(splitBySingleStatements[0], equivalents);
        }
    }

    @Test
    void testSelectStatements() throws IOException, JSQLParserException, XPathExpressionException, ParserConfigurationException, SAXException {
        this.parseTextFile(SupportedStatementType.SELECT);
        for(String key : this.equivalentStatements.keySet()){
            Pattern pattern = Pattern.compile(this.converterManagement.deparse(key, false, false));
            for(String toValidateStatements : this.equivalentStatements.get(key)){
                Matcher matcher = pattern.matcher(toValidateStatements);
//                Assertions.assertTrue(matcher.matches());
            }
        }
    }
}
