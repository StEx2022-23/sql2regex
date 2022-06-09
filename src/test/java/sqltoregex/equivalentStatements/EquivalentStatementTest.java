package sqltoregex.equivalentStatements;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import sqltoregex.ConverterManagement;
import sqltoregex.deparser.UserSettingsPreparer;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EquivalentStatementTest extends UserSettingsPreparer {
    private static final String DELIMITER_FOR_EQUIVALENTS = "#####";
    private static final String DELIMITER_FOR_SINGLE_STATEMENTS = ";";
    Map<String, List<String>> equivalentStatements;
    ConverterManagement converterManagement;

    public EquivalentStatementTest() throws XPathExpressionException, ParserConfigurationException, IOException,
            SAXException, URISyntaxException {
        super(SettingsType.ALL);
        converterManagement = new ConverterManagement(this.settingsManager);
    }

    void parseTextFile(SupportedStatementType statementType) throws IOException {
        equivalentStatements = new HashMap<>();
        String data = new String(
                Files.readAllBytes(Paths.get("src/test/resources/sqltoregex/equivalentStatements/" + statementType)));
        data = data.replaceAll("<!--.*--!>", "");
        data = data.replace("\r", "").replace("\n", "");
        String[] splitByEquivalents = data.split(DELIMITER_FOR_EQUIVALENTS);
        for (String str : splitByEquivalents) {
            String[] splitBySingleStatements = str.split(DELIMITER_FOR_SINGLE_STATEMENTS);
            List<String> equivalents = new ArrayList<>(
                    Arrays.asList(splitBySingleStatements).subList(1, splitBySingleStatements.length));
            this.equivalentStatements.put(splitBySingleStatements[0], equivalents);
        }
    }

    @Test
    void testSelectStatements() throws IOException, JSQLParserException {
        this.parseTextFile(SupportedStatementType.SELECT);
        for (String key : this.equivalentStatements.keySet()) {
            Pattern pattern = Pattern.compile(this.converterManagement.deparse(key, false, false, SettingsType.ALL));
            for (String toValidateStatements : this.equivalentStatements.get(key)) {
                Matcher matcher = pattern.matcher(toValidateStatements);
                Assertions.assertTrue(matcher.matches());
            }
        }
    }
}
