package sqltoregex.settings;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.RegExGenerator;

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
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SettingsManager {
    //refactor to a HashList of a new class "SettingsContainer" which holds Maps?
    private final Map<SettingsType, Map<SettingsOption, IRegExGenerator<?>>> settingsMap = new EnumMap<SettingsType,
            Map<SettingsOption, IRegExGenerator<?>>>(SettingsType.class);

    public SettingsManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException
            , URISyntaxException {
        this.parseSettings();
    }

    public static <T extends IRegExGenerator<?>> Optional<T> castSetting(IRegExGenerator<?> rawSetting,
                                                                            Class<T> clazz) {
        try {
            return Optional.of(clazz.cast(rawSetting));
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "Something went wrong by casting setting: {0}", e.toString());
        }
        return Optional.empty();
    }

    /**
     * Method for getting all Settings of one class with are setted by the user.
     */
    public <T extends IRegExGenerator<?>> Set<T> getSettingByClass(Class<T> clazz) {
        return getSettingByClass(clazz, SettingsType.USER);
    }

    public <T extends IRegExGenerator<?>> Set<T> getSettingByClass(Class<T> clazz,
                                                         SettingsType settingsType) {
        Set<T> settingsSet = new LinkedHashSet<>();
        for (IRegExGenerator<?> setting : this.getSettingsMap(settingsType).values()) {
            if (setting != null && setting.getClass().equals(clazz)) {
                castSetting(setting, clazz).ifPresent(settingsSet::add);
            }
        }
        return settingsSet;
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption) {
        return this.getSettingBySettingsOption(settingsOption, SettingsType.USER);
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption, SettingsType settingsType) {
        return this.getSettingsMap(settingsType).containsKey(settingsOption);
    }

    public <C extends IRegExGenerator<?>> Optional<C> getSettingBySettingsOption(SettingsOption settingsOption,
                                                                                    Class<C> clazz,
                                                                                    SettingsType settingsType) {
        for (Map.Entry<SettingsOption, IRegExGenerator<?>> entry
                : this.getSettingsMap(settingsType).entrySet()
        ) {
            if (entry.getKey().equals(settingsOption)) {
                return castSetting(entry.getValue(), clazz);
            }
        }
        return Optional.empty();
    }

    public Map<SettingsOption, IRegExGenerator<?>> getSettingsMap() {
        return this.getSettingsMap(SettingsType.USER);
    }

    public Map<SettingsOption, IRegExGenerator<?>> getSettingsMap(SettingsType settingsType) {
        Assert.notNull(settingsType, "settingsType must not be null");
        if (settingsType == SettingsType.USER) {
            return UserSettings.getInstance().getSettingsMap();
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

        Map<SettingsType, SettingsMapBuilder> settingsMapBuilderMap = new EnumMap<SettingsType, SettingsMapBuilder>(
                SettingsType.class);
        for (SettingsType settingsType : Arrays.stream(SettingsType.values())
                .filter(settingsType -> settingsType != SettingsType.USER).toList()) {
            settingsMapBuilderMap.put(settingsType, new SettingsMapBuilder());
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
                        settingsMapBuilderMap.get(SettingsType.valueOf(settingsTypeString.toUpperCase()))
                                .withNodeList(settingsElement.getElementsByTagName("value"), relatedOption);
                    }
                }
            }
        }
        for (SettingsType settingsType : Arrays.stream(SettingsType.values())
                .filter(settingsType -> settingsType != SettingsType.USER).toList()) {
            this.settingsMap.put(settingsType, settingsMapBuilderMap.get(settingsType).build());
        }
    }


    public Map<SettingsOption, IRegExGenerator<?>> parseUserSettingsInput(SettingsForm form) {
        SettingsMapBuilder settingsMapBuilder = new SettingsMapBuilder();

        Map<SettingsOption, IRegExGenerator<?>> map = settingsMapBuilder
                .withSettingsOptionSet(form.getSpellings())
                .withSettingsOptionSet(form.getOrders())
                .withSimpleDateFormatSet(form.getDateFormats(), SettingsOption.DATESYNONYMS)
                .withSimpleDateFormatSet(form.getTimeFormats(), SettingsOption.TIMESYNONYMS)
                .withSimpleDateFormatSet(form.getDateTimeFormats(), SettingsOption.DATETIMESYNONYMS)
                .withStringSet(form.getAggregateFunctionLang(), SettingsOption.AGGREGATEFUNCTIONLANG)
                .build();

        UserSettings.getInstance(map);

        return map;
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
