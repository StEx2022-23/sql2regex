package sqltoregex.api;

import net.sf.jsqlparser.JSQLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;
import sqltoregex.ConverterManagement;
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
 * Holds values from post api requests.
 */

public class ApiConvertObject {
    private List<String> sql;
    private SettingsType settingsType;
    private final List<String> regex;

    /**
     * Default constructor.
     */
    ApiConvertObject() {
        this.regex = new LinkedList<>();
    }

    /**
     * Gets current set multiple sql statements.
     * @return sql input as list of string
     */
    public List<String> getSql() {
        return this.sql;
    }

    /**
     * Sets multiple sql statements.
     * @param sql list of string with multiple sql statements
     */
    public void setSql(List<String> sql) {
        Assert.notNull(sql, "Minimum one SQL input is required.");
        if(sql.isEmpty()) throw new IllegalArgumentException("Minimum one SQL input is required.");
        this.sql = sql;
    }

    /**
     * Sets {@link SettingsType} for the converting process.
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
     * Gets current set {@link SettingsType}.
     * @return {@link SettingsType}
     */
    public SettingsType getSettingsType() {
        return this.settingsType;
    }

    /**
     * Gets solution from converting process.
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
            this.regex.add(ApiConverter.convert(str, this.settingsType));
        }
        return this.regex;
    }

    /**
     * Helper class to realize the converting process with an autowired ConverterManagement.
     */
    @Service
    private static class ApiConverter {
        private static ConverterManagement converterManagement;

        /**
         * Default constructor to inject the ConverterManagement
         * @param converterManagement {@link ConverterManagement}
         */
        @Autowired
        ApiConverter(ConverterManagement converterManagement){
            ApiConverter.converterManagement = converterManagement;
        }

        /**
         * Converts given sql to regex.
         * @param sql given sql statement
         * @param settingsType one of {@link SettingsType}
         * @return generated regex
         * @throws JSQLParserException if parsing goes wrong
         */
        public static String convert(String sql, SettingsType settingsType) throws JSQLParserException {
            return converterManagement.deparse(sql,false, settingsType);
        }
    }
}
