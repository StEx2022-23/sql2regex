package sqltoregex.settings;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sqltoregex.settings.regexgenerator.IRegExGenerator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Service
public class SettingsManager {
    private final Map<SettingsType, SettingsContainer> settingsMap = new EnumMap<>(SettingsType.class);

    public SettingsManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException
            , URISyntaxException {
        this.parseSettings();
    }

    public <T extends IRegExGenerator<?>> Set<T> getSettingByClass(Class<T> clazz,
                                                         SettingsType settingsType) {
        return new LinkedHashSet<>(this.getSettingsContainer(settingsType).get(clazz).values());
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption) {
        return this.getSettingBySettingsOption(settingsOption, SettingsType.USER);
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption, SettingsType settingsType) {
        return this.getSettingsContainer(settingsType).get(settingsOption) != null;
    }

    public <T extends IRegExGenerator<?>> Optional<T> getSettingBySettingsOption(SettingsOption settingsOption,
                                                                                    Class<T> clazz,
                                                                                    SettingsType settingsType) {
        for (T generator : this.getSettingsContainer(settingsType).get(clazz).values()){
            if (generator.getSettingsOption().equals(settingsOption)) {
                return Optional.of(generator);
            }
        }
        return Optional.empty();
    }

    public SettingsContainer getSettingsContainer() {
        return this.getSettingsContainer(SettingsType.USER);
    }

    public SettingsContainer getSettingsContainer(SettingsType settingsType) {
        Assert.notNull(settingsType, "settingsType must not be null");
        if (settingsType == SettingsType.USER) {
            return UserSettings.getInstance().getSettingsContainer();
        }
        return this.settingsMap.get(settingsType);
    }

    private void parseSettings() throws ParserConfigurationException, IOException, SAXException,
            XPathExpressionException, URISyntaxException {
        SettingsOption relatedOption;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        URL ressource = getClass().getClassLoader().getResource("static/config/properties.xml");
        assert ressource != null;
        Document document = builder.parse(String.valueOf(ressource.toURI()));
        document.getDocumentElement().normalize();

        stripWhitespaces(document);

        Map<SettingsType, SettingsContainer.Builder> settingsContainerBuilderMap = new EnumMap<>(
                SettingsType.class);
        for (SettingsType settingsType : Arrays.stream(SettingsType.values())
                .filter(settingsType -> settingsType != SettingsType.USER).toList()) {
            settingsContainerBuilderMap.put(settingsType, SettingsContainer.builder());
        }

        Node root = document.getElementsByTagName("properties").item(0);
        SettingsNodeListIterator categoryIterator = new SettingsNodeListIterator(root.getChildNodes());
        for (Node categoryNode : categoryIterator) {
            SettingsNodeListIterator settingsCategoryIterator = new SettingsNodeListIterator(
                    categoryNode.getChildNodes());
            for (Node settingsNode : settingsCategoryIterator) {
                relatedOption = SettingsOption.valueOf(settingsNode.getNodeName().toUpperCase());
                if (settingsNode instanceof Element settingsElement) {
                    String[] settingTypes = settingsElement
                            .getElementsByTagName("settingstype")
                            .item(0)
                            .getTextContent()
                            .split(";");
                    for (String settingsTypeString : settingTypes) {
                        settingsContainerBuilderMap.get(SettingsType.valueOf(settingsTypeString.toUpperCase()))
                                .withNodeList(settingsElement.getElementsByTagName("value"), relatedOption);
                    }
                }
            }
        }
        for (SettingsType settingsType : Arrays.stream(SettingsType.values())
                .filter(settingsType -> settingsType != SettingsType.USER).toList()) {
            this.settingsMap.put(settingsType, settingsContainerBuilderMap.get(settingsType).build());
        }
    }


    public SettingsContainer parseUserSettingsInput(SettingsForm form) {
        SettingsContainer.Builder settingsContainerBuilder = SettingsContainer.builder();

        SettingsContainer settingsContainer = settingsContainerBuilder
                .withSettingsOptionSet(form.getSpellings())
                .withSettingsOptionSet(form.getOrders())
                .withSimpleDateFormatSet(form.getDateFormats(), SettingsOption.DATESYNONYMS)
                .withSimpleDateFormatSet(form.getTimeFormats(), SettingsOption.TIMESYNONYMS)
                .withSimpleDateFormatSet(form.getDateTimeFormats(), SettingsOption.DATETIMESYNONYMS)
                .withStringSet(form.getAggregateFunctionLang(), SettingsOption.AGGREGATEFUNCTIONLANG)
                .withStringSet(form.getOtherSynonyms(), SettingsOption.OTHERSYNONYMS)
                .build();

        UserSettings.getInstance(settingsContainer);

        return settingsContainer;
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
