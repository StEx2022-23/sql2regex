package sqltoregex.settings;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

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
    private final Map<SettingsOption, RegExGenerator<?>> settingsMap = new EnumMap<>(SettingsOption.class);

    public SettingsManager() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, URISyntaxException {
        this.parseSettings();
    }

    public static <T> Optional<RegExGenerator<T>> castSetting(RegExGenerator<?> rawSetting,
                                                    Class<? extends RegExGenerator<T>> clazz) {
        try {
            return Optional.of(clazz.cast(rawSetting));
        } catch (ClassCastException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "Something went wrong by casting setting: {0}", e.toString());
        }
        return Optional.empty();
    }

    /**
     * Method for getting all OrderRotations, all Spellings, all Dateformats, allTime Formats.
     */
    public <S> Set<RegExGenerator<S>> getSettingByClass(Class<? extends RegExGenerator<S>> clazz) {
        return getSettingByClass(clazz, false);
    }

    public <S> Set<RegExGenerator<S>> getSettingByClass(Class<? extends RegExGenerator<S>> clazz, boolean useDefault) {
        Set<RegExGenerator<S>> settingsSet = new LinkedHashSet<>();
        for (RegExGenerator<?> setting :
                (useDefault ? this.getDefaultSettingsMap().values() : this.getSettingsMap().values())) {
            if (setting != null && setting.getClass().equals(clazz)) {
                castSetting(setting, clazz).ifPresent(settingsSet::add);
            }
        }
        return settingsSet;
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption) {
        return this.getSettingBySettingsOption(settingsOption, false);
    }

    public boolean getSettingBySettingsOption(SettingsOption settingsOption, boolean useDefault) {
        if (useDefault){
            return this.getDefaultSettingsMap().containsKey(settingsOption);
        }else {
            return this.getSettingsMap().containsKey(settingsOption);
        }
    }

    public <S> Optional<RegExGenerator<S>> getSettingBySettingsOption(SettingsOption settingsOption,
                                                            Class<? extends RegExGenerator<S>> clazz) {
        return this.getSettingBySettingsOption(settingsOption, clazz, false);
    }

    public <S> Optional<RegExGenerator<S>> getSettingBySettingsOption(SettingsOption settingsOption,
                                                            Class<? extends RegExGenerator<S>> clazz, boolean getAll) {
        for (Map.Entry<SettingsOption, RegExGenerator<?>> entry
                : (getAll ? this.getDefaultSettingsMap().entrySet() : this.getSettingsMap().entrySet())
        ) {
            if (entry.getKey().equals(settingsOption)) {
                return castSetting(settingsMap.get(entry.getKey()), clazz);
            }
        }
        return Optional.empty();
    }

    public Map<SettingsOption, RegExGenerator<?>> getDefaultSettingsMap(){
        return this.settingsMap;
    }

    public Map<SettingsOption, RegExGenerator<?>> getSettingsMap() {
        if (UserSettings.areSet()){
            return UserSettings.getInstance().getSettingsMap();
        }else{
            return this.settingsMap;
        }
    }

    public <A, S> Optional<SynonymGenerator<A, S>> getSynonymManagerBySettingOption(SettingsOption settingsOption,
                                                                          Class<? extends SynonymGenerator<A, S>> clazz) {
        return this.getSynonymManagerBySettingOption(settingsOption, clazz, false);
    }

    public <A, S> Optional<SynonymGenerator<A, S>> getSynonymManagerBySettingOption(SettingsOption settingsOption,
                                                                          Class<? extends SynonymGenerator<A, S>> clazz,
                                                                          boolean useDefault) {
        for (Map.Entry<SettingsOption, RegExGenerator<?>> entry
                : (useDefault ? this.getDefaultSettingsMap().entrySet() : this.getSettingsMap().entrySet())
        ) {
            List<SettingsOption> synonymManagerRelatedSettingsOptionsList = Arrays.asList(
                    SettingsOption.DATESYNONYMS,
                    SettingsOption.TIMESYNONYMS,
                    SettingsOption.DATETIMESYNONYMS,
                    SettingsOption.AGGREGATEFUNCTIONLANG
            );
            if (entry.getKey().equals(settingsOption) && synonymManagerRelatedSettingsOptionsList.contains(settingsOption)) {
                Optional<RegExGenerator<S>> opt = castSetting(settingsMap.get(entry.getKey()), clazz);
                return opt.map(sRegExGenerator -> (SynonymGenerator<A, S>) sRegExGenerator);
            }
        }
        throw new NoSuchElementException("There is no property with this property option:" + settingsOption);
    }

    private void parseSettings() throws ParserConfigurationException, IOException, SAXException,
            XPathExpressionException, URISyntaxException {
        SettingsOption relatedOption;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        URL ressource = getClass().getClassLoader().getResource("static/config/defaultProperties.xml");
        assert ressource != null;
        Document document = builder.parse(String.valueOf(ressource.toURI()));
        document.getDocumentElement().normalize();

        stripWhitespaces(document);
        SettingsMapBuilder mapBuilder = new SettingsMapBuilder();

        Node root = document.getElementsByTagName("properties").item(0);
        SettingsNodeListIterator categoryIterator = new SettingsNodeListIterator(root.getChildNodes());
        for (Node categoryNode : categoryIterator) {
            SettingsNodeListIterator settingsCategoryIterator = new SettingsNodeListIterator(
                    categoryNode.getChildNodes());
            for (Node settingsNode : settingsCategoryIterator) {
                relatedOption = SettingsOption.valueOf(settingsNode.getNodeName().toUpperCase());
                mapBuilder.withNodeList(settingsNode.getChildNodes(), relatedOption);
            }
        }
        this.settingsMap.putAll(mapBuilder.build());
    }


    public Map<SettingsOption, RegExGenerator<?>> parseUserSettingsInput(SettingsForm form) {
        SettingsMapBuilder settingsMapBuilder = new SettingsMapBuilder();

        Map<SettingsOption, RegExGenerator<?>> map = settingsMapBuilder
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
