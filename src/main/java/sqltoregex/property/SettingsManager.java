package sqltoregex.property;

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
    private static final String PROPERTY_DEACTIVATED = "false";
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

    public Map<SettingsOption, RegExGenerator<?, ?>> parseUserOptionsInput(SettingsForm form){
        SettingsMapBuilder settingsMapBuilder = new SettingsMapBuilder();

        return settingsMapBuilder
                .withPropertyOptionSet(form.getSpellings())
                .withPropertyOptionSet(form.getOrders())
                .withSimpleDateFormatSet(form.getDateFormats(), SettingsOption.DATESYNONYMS)
                .withSimpleDateFormatSet(form.getTimeFormats(), SettingsOption.TIMESYNONYMS)
                .withSimpleDateFormatSet(form.getDateTimeFormats(), SettingsOption.DATETIMESYNONYMS)
                .withStringSet(form.getAggregateFunctionLang(), SettingsOption.AGGREGATEFUNCTIONLANG)
                .build();
    }

    private SettingsMapBuilder addPropertyToMap(Map<SettingsOption, NodeList> parsedValues) {
        SettingsMapBuilder settingsMapBuilder = new SettingsMapBuilder();
        for(SettingsOption prop : SettingsOption.values()){
            if (parsedValues.containsKey(prop) && parsedValues.get(prop).item(0).getTextContent().equals(PROPERTY_DEACTIVATED)) {
                return settingsMapBuilder;
            }
            switch (prop) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, TABLENAMEORDER, COLUMNNAMEORDER -> settingsMapBuilder.withPropertyOption(prop);
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    Set<String> valueList = new HashSet<>();
                    SettingsNodeListIterator settingsNodeListIterator = new SettingsNodeListIterator(parsedValues.get(prop));
                    for(Node node : settingsNodeListIterator){
                        valueList.add(node.getTextContent());
                    }
                    settingsMapBuilder.withStringSet(valueList, prop);
                }
                case AGGREGATEFUNCTIONLANG -> {
                    List<Node> valuePairsForSynonyms = new LinkedList<>();
                    SettingsNodeListIterator valueTagIterator = new SettingsNodeListIterator(parsedValues.get(prop));
                    for(Node node : valueTagIterator){
                        valuePairsForSynonyms.add(node);
                    }
                    Set<String> pairOfSynonymList = new HashSet<>();
                    for(Node valueNode : valuePairsForSynonyms){
                        String valuePair = valueNode.getTextContent();
                        pairOfSynonymList.add(valuePair);
                    }
                    settingsMapBuilder.withStringSet(pairOfSynonymList, SettingsOption.AGGREGATEFUNCTIONLANG);
                }
                case NOT_AS_EXCLAMATION_AND_WORD -> settingsMapBuilder.withPropertyOption(SettingsOption.NOT_AS_EXCLAMATION_AND_WORD);
                case DEFAULT -> {}
                default -> {
                    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                    logger.log(Level.INFO, "Something went wrong by adding a property to the map.");
                }
            }
        }
        return settingsMapBuilder;
    }

    /**
     * Method for getting all OrderRotations, all Spellings, all Dateformats, allTime Formats.
     */
    public <S, R> Set<RegExGenerator<S,R>> getSettingByClass(Class<? extends RegExGenerator<S,R>> clazz) {
        Set<RegExGenerator<S,R>> propertySet = new LinkedHashSet<>();
        for (RegExGenerator<?, ?> property :
                this.settingsMap.values()) {
            if (property.getClass().equals(clazz)) {
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

        Map<SettingsOption, NodeList> parsedValues = new EnumMap<>(SettingsOption.class);
        Node root = document.getElementsByTagName("properties").item(0);
        NodeList properties = root.getChildNodes();
        SettingsNodeListIterator rootElementIterator = new SettingsNodeListIterator(properties);
        for (Node rootNode : rootElementIterator) {
            NodeList propertyCategory = rootNode.getChildNodes();
            SettingsNodeListIterator propertyCategoryIterator = new SettingsNodeListIterator(propertyCategory);
            for (Node categoryNode : propertyCategoryIterator) {
                relatedOption = SettingsOption.valueOf(categoryNode.getNodeName().toUpperCase());
                NodeList innerNodes = categoryNode.getChildNodes();
                parsedValues.put(relatedOption, innerNodes);
            }
        }
        this.settingsMap.putAll(this.addPropertyToMap(parsedValues).build());
    }

    public Set<SettingsOption> readPropertyOptions() {
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
