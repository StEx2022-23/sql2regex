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
public class PropertyManager {
    private static final String PROPERTY_DEACTIVATED = "false";
    private final Map<PropertyOption, RegExGenerator<?, ?>> propertyMap = new EnumMap<>(PropertyOption.class);


    public PropertyManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        this.parseProperties();
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

    public Map<PropertyOption, RegExGenerator<?, ?>> parseUserOptionsInput(PropertyForm form){
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();

        return propertyMapBuilder
                .withPropertyOptionSet(form.getSpellings())
                .withPropertyOptionSet(form.getOrders())
                .withSimpleDateFormatSet(form.getDateFormats(), PropertyOption.DATESYNONYMS)
                .withSimpleDateFormatSet(form.getTimeFormats(), PropertyOption.TIMESYNONYMS)
                .withSimpleDateFormatSet(form.getDateTimeFormats(), PropertyOption.DATETIMESYNONYMS)
                .withStringSet(form.getAggregateFunctionLang(), PropertyOption.AGGREGATEFUNCTIONLANG)
                .build();
    }

    private PropertyMapBuilder addPropertyToMap(Map<PropertyOption, NodeList> parsedValues) {
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        for(PropertyOption prop : PropertyOption.values()){
            if (parsedValues.containsKey(prop) && parsedValues.get(prop).item(0).getTextContent().equals(PROPERTY_DEACTIVATED)) {
                return propertyMapBuilder;
            }
            switch (prop) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, TABLENAMEORDER, COLUMNNAMEORDER -> propertyMapBuilder.withPropertyOption(prop);
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    Set<String> valueList = new HashSet<>();
                    PropertyNodeListIterator propertyNodeListIterator = new PropertyNodeListIterator(parsedValues.get(prop));
                    for(Node node : propertyNodeListIterator){
                        valueList.add(node.getTextContent());
                    }
                    propertyMapBuilder.withStringSet(valueList, prop);
                }
                case AGGREGATEFUNCTIONLANG -> {
                    List<Node> valuePairsForSynonyms = new LinkedList<>();
                    PropertyNodeListIterator valueTagIterator = new PropertyNodeListIterator(parsedValues.get(prop));
                    for(Node node : valueTagIterator){
                        valuePairsForSynonyms.add(node);
                    }
                    Set<String> pairOfSynonymList = new HashSet<>();
                    for(Node valueNode : valuePairsForSynonyms){
                        String valuePair = valueNode.getTextContent();
                        pairOfSynonymList.add(valuePair);
                    }
                    propertyMapBuilder.withStringSet(pairOfSynonymList, PropertyOption.AGGREGATEFUNCTIONLANG);
                }
                default -> {
                    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                    logger.log(Level.INFO, "Something went wrong by adding a property to the map.");
                }
            }
        }
        return propertyMapBuilder;
    }

    /**
     * Method for getting all OrderRotations, all Spellings, all Dateformats, allTime Formats.
     */
    public <S, R> Set<RegExGenerator<S,R>> getPropertyByClass(Class<? extends RegExGenerator<S,R>> clazz) {
        Set<RegExGenerator<S,R>> propertySet = new LinkedHashSet<>();
        for (RegExGenerator<?, ?> property :
                this.propertyMap.values()) {
            if (property.getClass().equals(clazz)) {
                propertySet.add(castProperty(property, clazz));
            }
        }
        return propertySet;
    }

    public <S, R> RegExGenerator<S,R> getPropertyByPropOption(PropertyOption propertyOption, Class<? extends RegExGenerator<S,R>> clazz) {
        for (Map.Entry<PropertyOption, RegExGenerator<?, ?>> entry : this.propertyMap.entrySet()) {
            if (entry.getKey().equals(propertyOption)) {
                return castProperty(propertyMap.get(entry.getKey()), clazz);
            }
        }
        throw new NoSuchElementException("There is no property with this property option:" + propertyOption);
    }

    public Map<PropertyOption, RegExGenerator<?, ?>> getPropertyMap() {
        return this.propertyMap;
    }

    private void parseProperties() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        PropertyOption relatedOption;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse("src/main/resources/static/config/defaultProperties.xml");
        document.getDocumentElement().normalize();

        stripWhitespaces(document);

        Map<PropertyOption, NodeList> parsedValues = new EnumMap<>(PropertyOption.class);
        Node root = document.getElementsByTagName("properties").item(0);
        NodeList properties = root.getChildNodes();
        PropertyNodeListIterator rootElementIterator = new PropertyNodeListIterator(properties);
        for (Node rootNode : rootElementIterator) {
            NodeList propertyCategory = rootNode.getChildNodes();
            PropertyNodeListIterator propertyCategoryIterator = new PropertyNodeListIterator(propertyCategory);
            for (Node categoryNode : propertyCategoryIterator) {
                relatedOption = PropertyOption.valueOf(categoryNode.getNodeName().toUpperCase());
                NodeList innerNodes = categoryNode.getChildNodes();
                parsedValues.put(relatedOption, innerNodes);
            }
        }
        this.propertyMap.putAll(this.addPropertyToMap(parsedValues).build());
    }

    public Set<PropertyOption> readPropertyOptions() {
        return this.propertyMap.keySet();
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
