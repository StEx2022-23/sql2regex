package sqltoregex.api;

import net.sf.jsqlparser.JSQLParserException;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;
import sqltoregex.ConverterManagement;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hold values from post api requests.
 */
public class ApiConvertObject {
    private List<String> sql;
    private SettingsType settingsType;
    private final List<String> regex;

    /**
     * Default constructor.
     */
    public ApiConvertObject() {
        this.regex = new LinkedList<>();
    }

    /**
     * Get current set multiple sql statements.
     * @return sql input as list of string
     */
    public List<String> getSql() {
        return this.sql;
    }

    /**
     * Set multiple sql statements.
     * @param sql list of string with multiple sql statements
     */
    public void setSql(List<String> sql) {
        Assert.notNull(sql, "Minimum one SQL input is required.");
        if(sql.isEmpty()) throw new IllegalArgumentException("Minimum one SQL input is required.");
        this.sql = sql;
    }

    /**
     * Set {@link SettingsType} for the converting process.
     * @param settingsType {@link SettingsType}
     */
    public void setSettingsType(SettingsType settingsType) {
        if(settingsType == null){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "No settings type given. Uses default settings.");
            this.settingsType = SettingsType.ALL;
        } else {
            this.settingsType = settingsType;
        }
    }

    /**
     * Set {@link SettingsType} for the converting process from a input {@link String}.
     * @param settingsType {@link String} represent the {@link SettingsType}
     */
    public void setSettingsType(String settingsType) {
        if(settingsType.isEmpty()){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "No settings type given. Uses default settings.");
            this.settingsType = SettingsType.ALL;
        } else {
            this.settingsType = SettingsType.valueOf(settingsType);
        }
    }

    /**
     * Get current set {@link SettingsType}.
     * @return {@link SettingsType}
     */
    public SettingsType getSettingsType() {
        return this.settingsType;
    }

    /**
     * Get solution from converting process.
     * @return list of string with generated regex
     * @throws XPathExpressionException if xml parsing error occurs
     * @throws ParserConfigurationException if xml parsing error occurs
     * @throws IOException if xml parsing error occurs
     * @throws URISyntaxException if xml parsing error occurs
     * @throws SAXException if xml parsing error occurs
     * @throws JSQLParserException if xml parsing error occurs
     */
    public List<String> getRegex() throws XPathExpressionException, ParserConfigurationException, IOException, URISyntaxException, SAXException, JSQLParserException {
        for(String str : this.sql){
            this.regex.add(new ConverterManagement(new SettingsManager()).deparse(str, false, true, this.settingsType));
        }
        return this.regex;
    }
}
