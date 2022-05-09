package sqltoregex.property;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PropertyManager {
    private final Map<PropertyOptions, Property> propertyMap = new HashMap<>();

    public Set<PropertyOptions> readPropertyOptions(){
        return this.propertyMap.keySet();
    }

    private void addPropertyToMap(List<String> valueList, String relatedOption){
        switch (relatedOption) {
            case "keywordspelling" -> {
                if(valueList.get(0).equals("false")){break;}
                propertyMap.put(PropertyOptions.keywordspelling, new SpellingMistake());
            } case "tablenameorder" -> {
                if (valueList.get(0).equals("false")) {break;}
                propertyMap.put(PropertyOptions.tablenameorder, new OrderRotation());
            } case "columnnameorder" -> {
                if(valueList.get(0).equals("false")){break;}
                propertyMap.put(PropertyOptions.columnnameorder, new OrderRotation());
            } case "datesynonyms" -> {
                if(valueList.get(0).equals("false")){break;}
                DateSynonymManager dateSynonymManager = new DateSynonymManager();
                for(String dateformat : valueList){
                    dateSynonymManager.addSynonym(new SimpleDateFormat(dateformat));
                }
                propertyMap.put(PropertyOptions.datesynonyms, dateSynonymManager);
            }
        }
    }

    public void parseProperties() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String relatedOption;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse("src/main/java/sqltoregex/property/defaultProperties.xml");
        document.getDocumentElement().normalize();

        Node root = document.getElementsByTagName("properties").item(0);
        NodeList properties = root.getChildNodes();
        PropertyNodeListIterator rootElementIterator = new PropertyNodeListIterator(properties);
        for(Node rootNodes : rootElementIterator) {
            NodeList propertyCategory = rootNodes.getChildNodes();
            PropertyNodeListIterator propertyCategoryIterator = new PropertyNodeListIterator(propertyCategory);
            for (Node categoryNode : propertyCategoryIterator) {
                relatedOption = categoryNode.getNodeName();
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

    public Map<String, Property> calculateUserDefinedPropertyList(Map<PropertyOptions, List<String>> userDefinitions){
        return null;
    }

    public  Map<PropertyOptions, Property> getPropertyMap() {
        return this.propertyMap;
    }
}
