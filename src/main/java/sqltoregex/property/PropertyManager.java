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
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PropertyManager {
    private final Map<PropertyOption, Property> propertyMap = new EnumMap<PropertyOption, Property>(PropertyOption.class);
    private static final String PROPERTY_DEACTIVATED = "false";

    public PropertyManager() throws ParserConfigurationException, IOException, ClassNotFoundException, InvocationTargetException, SAXException, NoSuchMethodException, InstantiationException, IllegalAccessException, XPathExpressionException {
        this.parseProperties();
    }

    public Set<PropertyOption> readPropertyOptions(){
        return this.propertyMap.keySet();
    }

    private void addPropertyToMap(List<String> valueList, PropertyOption relatedOption){
        switch (relatedOption) {
            case KEYWORDSPELLING -> {
                if(valueList.get(0).equals(PROPERTY_DEACTIVATED)){break;}
                propertyMap.put(PropertyOption.KEYWORDSPELLING, new SpellingMistake());
            } case TABLENAMEORDER -> {
                if (valueList.get(0).equals(PROPERTY_DEACTIVATED)) {break;}
                propertyMap.put(PropertyOption.TABLENAMEORDER, new OrderRotation());
            } case COLUMNNAMEORDER -> {
                if(valueList.get(0).equals(PROPERTY_DEACTIVATED)){break;}
                propertyMap.put(PropertyOption.COLUMNNAMEORDER, new OrderRotation());
            } case DATESYNONYMS -> {
                if(valueList.get(0).equals(PROPERTY_DEACTIVATED)){break;}
                DateAndTimeFormatSynonymGenerator dateSynonymManager = new DateAndTimeFormatSynonymGenerator();
                for(String dateformat : valueList){
                    dateSynonymManager.addSynonym(new SimpleDateFormat(dateformat));
                }
                propertyMap.put(PropertyOption.DATESYNONYMS, dateSynonymManager);
            }
        }
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

        Node root = document.getElementsByTagName("properties").item(0);
        NodeList properties = root.getChildNodes();
        PropertyNodeListIterator rootElementIterator = new PropertyNodeListIterator(properties);
        for(Node rootNode : rootElementIterator) {
            NodeList propertyCategory = rootNode.getChildNodes();
            PropertyNodeListIterator propertyCategoryIterator = new PropertyNodeListIterator(propertyCategory);
            for (Node categoryNode : propertyCategoryIterator) {
                relatedOption = PropertyOption.valueOf(categoryNode.getNodeName().toUpperCase());
                NodeList innerNodes = categoryNode.getChildNodes();
                PropertyNodeListIterator innerNodesIterator = new PropertyNodeListIterator(innerNodes);
                List<String> valueList = new ArrayList<>();
                for (Node innerNode : innerNodesIterator) {
                    valueList.add(innerNode.getTextContent());
                }
                this.addPropertyToMap(valueList, relatedOption);
            }
        }
    }

    public  Map<PropertyOption, Property> getPropertyMap() {
        return this.propertyMap;
    }

    /**
     * Removes insignificant whitespaces from an XML DOM tree.
     * @param doc
     * @throws XPathExpressionException
     */
    private void stripWhitespaces(Document doc) throws XPathExpressionException {
        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
        for (int i=0; i < nl.getLength(); ++i) {
            Node node = nl.item(i);
            node.getParentNode().removeChild(node);
        }
    }
}
