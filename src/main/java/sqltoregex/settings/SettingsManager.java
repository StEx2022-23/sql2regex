package sqltoregex.settings;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SettingsManager {
    private final Map<SettingsOption, RegExGenerator<?, ?>> settingsMap = new EnumMap<>(SettingsOption.class);


    public SettingsManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        this.parseSettings();
    }

    public static <S, R> RegExGenerator<S,R> castProperty(RegExGenerator<?, ?> rawProperty, Class<? extends RegExGenerator<S, R>> clazz) {
        try {
            return clazz.cast(rawProperty);
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "Something went wrong by casting property: {0}", e.toString());
        }
        return null;
    }

    public Map<SettingsOption, RegExGenerator<?, ?>> parseUserSettingsInput(SettingsForm form){
        SettingsMapBuilder settingsMapBuilder = new SettingsMapBuilder();

        return settingsMapBuilder
                .withSettingsOptionSet(form.getSpellings())
                .withSettingsOptionSet(form.getOrders())
                .withSimpleDateFormatSet(form.getDateFormats(), SettingsOption.DATESYNONYMS)
                .withSimpleDateFormatSet(form.getTimeFormats(), SettingsOption.TIMESYNONYMS)
                .withSimpleDateFormatSet(form.getDateTimeFormats(), SettingsOption.DATETIMESYNONYMS)
                .withStringSet(form.getAggregateFunctionLang(), SettingsOption.AGGREGATEFUNCTIONLANG)
                .build();
    }

    /**
     * Method for getting all OrderRotations, all Spellings, all Dateformats, allTime Formats.
     */
    public <S, R> Set<RegExGenerator<S,R>> getSettingByClass(Class<? extends RegExGenerator<S,R>> clazz) {
        Set<RegExGenerator<S,R>> propertySet = new LinkedHashSet<>();
        for (RegExGenerator<?, ?> property :
                this.settingsMap.values()) {
            if (property != null && property.getClass().equals(clazz)) {
                propertySet.add(castProperty(property, clazz));
            }
        }
        return propertySet;
    }

    public <S, R> RegExGenerator<S,R> getSettingBySettingOption(SettingsOption settingsOption, Class<? extends RegExGenerator<S,R>> clazz) {
        for (Map.Entry<SettingsOption, RegExGenerator<?, ?>> entry : this.settingsMap.entrySet()) {
            if (entry.getKey().equals(settingsOption)) {
                return castProperty(settingsMap.get(entry.getKey()), clazz);
            }
        }
        throw new NoSuchElementException("There is no property with this property option:" + settingsOption);
    }

    public boolean getSettingBySettingOption(SettingsOption settingsOption){
        return this.settingsMap.containsKey(settingsOption);
    }

    public Map<SettingsOption, RegExGenerator<?, ?>> getSettingsMap() {
        return this.settingsMap;
    }

    private void parseSettings() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        SettingsOption relatedOption;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse("src/main/resources/static/config/defaultProperties.xml");
        document.getDocumentElement().normalize();

        stripWhitespaces(document);
        SettingsMapBuilder mapBuilder = new SettingsMapBuilder();

        Node root = document.getElementsByTagName("properties").item(0);
        SettingsNodeListIterator categoryIterator = new SettingsNodeListIterator(root.getChildNodes());
        for (Node categoryNode : categoryIterator) {
            NodeList propertyCategory = categoryNode.getChildNodes();
            SettingsNodeListIterator propertyCategoryIterator = new SettingsNodeListIterator(propertyCategory);
            for (Node settingsNode : propertyCategoryIterator) {
                relatedOption = SettingsOption.valueOf(settingsNode.getNodeName().toUpperCase());
                mapBuilder.withNodeList(settingsNode.getChildNodes(), relatedOption);
            }
        }
        this.settingsMap.putAll(mapBuilder.build());
    }

    public Set<SettingsOption> getDefaultSettings() {
        return this.settingsMap.keySet();
    }

    /**
     * Removes insignificant whitespaces from an XML DOM tree.
     *
     * @param doc Document
     * @throws XPathExpressionException if xp.evaluate("//text()[normalize-space(.)='']" goes wrong
     */
    private void stripWhitespaces(Document doc) throws XPathExpressionException {
        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); ++i) {
            Node node = nl.item(i);
            node.getParentNode().removeChild(node);
        }
    }


}
