package sqltoregex.settings;

import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

class SettingsMapBuilder {
    private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
    private static final String STRING_SYNONYM_DELIMITER = ";";
    private final Map<SettingsOption, IRegExGenerator<?>> settingsMap;

    public SettingsMapBuilder() {
        this.settingsMap = new EnumMap<>(SettingsOption.class);
    }

    public Map<SettingsOption, IRegExGenerator<?>> build() {
        return this.settingsMap;
    }

    public SettingsMapBuilder withNodeList(NodeList nodeList, SettingsOption settingsOption) {
        if (nodeList.item(0).getTextContent().equals("false")) {
            return this;
        }

        switch (settingsOption) {
            //TODO: add not as eclamation mark after refactoring
            case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, INDEXCOLUMNNAMESPELLING, TABLENAMEORDER, COLUMNNAMEORDER, GROUPBYELEMENTORDER, INDEXCOLUMNNAMEORDER -> this.withSettingsOption(
                    settingsOption);
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                Set<String> valueList = new HashSet<>();
                SettingsNodeListIterator settingsNodeListIterator = new SettingsNodeListIterator(nodeList);
                for (Node node : settingsNodeListIterator) {
                    valueList.add(node.getTextContent());
                }
                this.withStringSet(valueList, settingsOption);
            }
            case AGGREGATEFUNCTIONLANG, OTHERSYNONYMS -> {
                List<Node> valuePairsForSynonyms = new LinkedList<>();
                SettingsNodeListIterator valueTagIterator = new SettingsNodeListIterator(nodeList);
                for (Node node : valueTagIterator) {
                    valuePairsForSynonyms.add(node);
                }
                Set<String> pairOfSynonymList = new HashSet<>();
                for (Node valueNode : valuePairsForSynonyms) {
                    String valuePair = valueNode.getTextContent();
                    pairOfSynonymList.add(valuePair);
                }
                this.withStringSet(pairOfSynonymList, settingsOption);
            }
            case DEFAULT -> {
                //pass because nothing needs to be added for default
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withSettingsOption(SettingsOption settingsOption) {
        switch (settingsOption) {
            case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, INDEXCOLUMNNAMESPELLING -> {
                SpellingMistake spellingMistake = new SpellingMistake(settingsOption);
                this.settingsMap.put(settingsOption, spellingMistake);
            }
            case TABLENAMEORDER, COLUMNNAMEORDER, INDEXCOLUMNNAMEORDER, GROUPBYELEMENTORDER -> {
                OrderRotation orderRotation = new OrderRotation(settingsOption);
                this.settingsMap.put(settingsOption, orderRotation);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withSettingsOptionSet(Set<SettingsOption> settingsOptions) {
        Assert.notNull(settingsOptions, "Set of settings options must not be null");
        for (SettingsOption settingsOption : settingsOptions) {
            withSettingsOption(settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withSimpleDateFormatSet(Set<SimpleDateFormat> synonyms, SettingsOption settingsOption) {
        Assert.notNull(synonyms, "Set of simple date formats options must not be null");
        switch (settingsOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                        settingsOption);
                for (SimpleDateFormat format : synonyms) {
                    synonymGenerator.addSynonym(format);
                }
                this.settingsMap.put(settingsOption, synonymGenerator);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withStringSet(Set<String> synonyms, SettingsOption settingsOption) {
        switch (settingsOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                        settingsOption);
                for (String format : synonyms) {
                    synonymGenerator.addSynonym(new SimpleDateFormat(format));
                }
                this.settingsMap.put(settingsOption, synonymGenerator);
            }
            case AGGREGATEFUNCTIONLANG, OTHERSYNONYMS -> {
                if (synonyms != null && !synonyms.isEmpty()) {
                    StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(
                            settingsOption);
                    for (String singleSynonym : synonyms) {
                        aggregateFunctionSynonymGenerator.addSynonymFor(
                                singleSynonym.split(STRING_SYNONYM_DELIMITER)[0].strip(),
                                singleSynonym.split(STRING_SYNONYM_DELIMITER)[1].strip());
                    }
                    this.settingsMap.put(settingsOption, aggregateFunctionSynonymGenerator);
                }
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }
}
