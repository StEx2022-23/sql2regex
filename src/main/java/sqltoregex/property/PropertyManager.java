package sqltoregex.property;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PropertyManager {
    private static final String PROPERTY_DEACTIVATED = "false";
    private final Map<PropertyOption, Property<?>> propertyMap = new EnumMap<>(PropertyOption.class);


    public PropertyManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        this.parseProperties();
    }

    public static <T> Property<T> castProperty(Property<?> rawProperty, Class<? extends Property<T>> clazz) {
        try {
            return clazz.cast(rawProperty);
        } catch (ClassCastException e) {
            //think about logging ...
        }
        return null;
    }

    private Set<String> setOfSimpleDateFormatToSetOfString(Set<SimpleDateFormat> simpleDateFormatList){
        if(simpleDateFormatList.isEmpty()){
            throw new IllegalArgumentException("Set is not allowed to be empty.");
        }
        Set<String> simpledDateFormatToString = new HashSet<>();
        for(SimpleDateFormat simpleDateFormat : simpleDateFormatList){
            simpledDateFormatToString.add(simpleDateFormat.toPattern());
        }
        return simpledDateFormatToString;
    }

    public Map<PropertyOption, Property<?>> parseUserOptionsInput(PropertyForm form){
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();

        Set<PropertyOption> spellingsFromForm = form.getSpellings();
        if(spellingsFromForm != null && !spellingsFromForm.isEmpty()){
            for(PropertyOption propertyOption : spellingsFromForm){
                propertyMapBuilder.with(propertyOption);
            }
        }

        Set<PropertyOption> ordersFromForm = form.getOrders();
        if(ordersFromForm != null && !ordersFromForm.isEmpty()) {
            for(PropertyOption propertyOption : ordersFromForm){
                propertyMapBuilder.with(propertyOption);
            }
        }

        Set<SimpleDateFormat> dateFormatFromForm = form.getDateFormats();
        if(dateFormatFromForm != null && !dateFormatFromForm.isEmpty()) {
            propertyMapBuilder.with(setOfSimpleDateFormatToSetOfString(dateFormatFromForm), PropertyOption.DATESYNONYMS);
        }

        Set<SimpleDateFormat> timeFormatFromForm = form.getTimeFormats();
        if(timeFormatFromForm != null && !timeFormatFromForm.isEmpty()) {
            propertyMapBuilder.with(setOfSimpleDateFormatToSetOfString(timeFormatFromForm), PropertyOption.TIMESYNONYMS);
        }

        Set<SimpleDateFormat> dateTimeFormatFromForm = form.getDateTimeFormats();
        if(dateTimeFormatFromForm != null && !dateTimeFormatFromForm.isEmpty()) {
            propertyMapBuilder.with(setOfSimpleDateFormatToSetOfString(dateTimeFormatFromForm), PropertyOption.DATETIMESYNONYMS);
        }

        Set<String> synonymsListForAggregateFunctionsAsStrings = form.getAggregateFunctionLang();
        if(synonymsListForAggregateFunctionsAsStrings != null && !synonymsListForAggregateFunctionsAsStrings.isEmpty()) {
            propertyMapBuilder.with(synonymsListForAggregateFunctionsAsStrings, PropertyOption.AGGREGATEFUNCTIONLANG);
        }

        return propertyMapBuilder.build();
    }

    private PropertyMapBuilder addPropertyToMap(Map<PropertyOption, NodeList> parsedValues) {
        PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
        for(PropertyOption prop : PropertyOption.values()){
            if (parsedValues.containsKey(prop) && parsedValues.get(prop).item(0).getTextContent().equals(PROPERTY_DEACTIVATED)) {
                return propertyMapBuilder;
            }
            switch (prop) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, TABLENAMEORDER, COLUMNNAMEORDER -> propertyMapBuilder.with(prop);
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    Set<String> valueList = new HashSet<>();
                    PropertyNodeListIterator propertyNodeListIterator = new PropertyNodeListIterator(parsedValues.get(prop));
                    for(Node node : propertyNodeListIterator){
                        valueList.add(node.getTextContent());
                    }
                    propertyMapBuilder.with(valueList, prop);
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
                    propertyMapBuilder.with(pairOfSynonymList, PropertyOption.AGGREGATEFUNCTIONLANG);
                }
            }
        }
        return propertyMapBuilder;
    }

    /**
     * Method for getting all OrderRotations, all Spellings, all Dateformats, allTime Formats.
     */
    public <T> Set<Property<T>> getPropertyByClass(Class<? extends Property<T>> clazz) {
        Set<Property<T>> propertySet = new LinkedHashSet<>();
        for (Property<?> property :
                this.propertyMap.values()) {
            if (property.getClass().equals(clazz)) {
                propertySet.add(castProperty(property, clazz));
            }
        }
        return propertySet;
    }

    public <T> Property<T> getPropertyByPropOption(PropertyOption propertyOption, Class<? extends Property<T>> clazz) {
        for (Map.Entry<PropertyOption, Property<?>> entry : this.propertyMap.entrySet()) {
            if (entry.getKey().equals(propertyOption)) {
                return castProperty(propertyMap.get(entry.getKey()), clazz);
            }
        }
        throw new NoSuchElementException("There is no property with this property option:" + propertyOption);
    }

    public Map<PropertyOption, Property<?>> getPropertyMap() {
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
     * @param doc delete whitespace nodes
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
