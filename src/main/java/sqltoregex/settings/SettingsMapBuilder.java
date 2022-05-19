package sqltoregex.settings;

import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

class SettingsMapBuilder {
    private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
    private static final String STRING_SYNONYM_DELIMITER = ";";
    private final Set<OrderRotation> ORDER_ROTATIONS;
    private final Map<SettingsOption, RegExGenerator<?>> SETTINGS_MAP;
    private final Set<SpellingMistake> SPELLING_MISTAKES;

    public SettingsMapBuilder() {
        this.SETTINGS_MAP = new EnumMap<>(SettingsOption.class);
        this.ORDER_ROTATIONS = new LinkedHashSet<>();
        this.SPELLING_MISTAKES = new LinkedHashSet<>();
    }

    public Map<SettingsOption, RegExGenerator<?>> build() {
        for (OrderRotation orderRotation : ORDER_ROTATIONS) {
            SettingsOption settingsOption = orderRotation.getSettingsOption();
            SpellingMistake spellingMistake = new SpellingMistake(SettingsOption.valueOf(
                    settingsOption.toString().substring(0, settingsOption.toString().length() - 5) + "SPELLING"));
            if (SPELLING_MISTAKES.contains(spellingMistake)) {
                orderRotation.setSpellingMistake(spellingMistake);
            }
        }
        return this.SETTINGS_MAP;
    }

    public SettingsMapBuilder withNodeList(NodeList nodeList, SettingsOption settingsOption) {
        if (nodeList.item(0).getTextContent().equals("false")) {
            return this;
        }

        switch (settingsOption) {
            case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, TABLENAMEORDER, COLUMNNAMEORDER -> this.withSettingsOption(
                    settingsOption);
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                Set<String> valueList = new HashSet<>();
                SettingsNodeListIterator settingsNodeListIterator = new SettingsNodeListIterator(nodeList);
                for (Node node : settingsNodeListIterator) {
                    valueList.add(node.getTextContent());
                }
                this.withStringSet(valueList, settingsOption);
            }
            case AGGREGATEFUNCTIONLANG -> {
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
                this.withStringSet(pairOfSynonymList, SettingsOption.AGGREGATEFUNCTIONLANG);
            }
            case NOT_AS_EXCLAMATION_AND_WORD -> this.withSettingsOption(SettingsOption.NOT_AS_EXCLAMATION_AND_WORD);
            case DEFAULT -> {
                //pass because nothing needs to be needed for default
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withSettingsOption(SettingsOption settingsOption) {
        switch (settingsOption) {
            case KEYWORDSPELLING, COLUMNNAMESPELLING, TABLENAMESPELLING -> {
                SpellingMistake spellingMistake = new SpellingMistake(settingsOption);
                this.SETTINGS_MAP.put(settingsOption, spellingMistake);
                SPELLING_MISTAKES.add(spellingMistake);
            }
            case TABLENAMEORDER, COLUMNNAMEORDER -> {
                OrderRotation orderRotation = new OrderRotation(settingsOption);
                this.SETTINGS_MAP.put(settingsOption, orderRotation);
                ORDER_ROTATIONS.add(orderRotation);
            }
            case NOT_AS_EXCLAMATION_AND_WORD -> this.SETTINGS_MAP.put(settingsOption, null);
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
                this.SETTINGS_MAP.put(settingsOption, synonymGenerator);
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }

    public SettingsMapBuilder withStringSet(Set<String> synonyms, SettingsOption settingsOption) {
        Assert.notNull(synonyms, "Set of strings options must not be null");
        switch (settingsOption) {
            case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                        settingsOption);
                for (String format : synonyms) {
                    synonymGenerator.addSynonym(new SimpleDateFormat(format));
                }
                this.SETTINGS_MAP.put(settingsOption, synonymGenerator);
            }
            case AGGREGATEFUNCTIONLANG -> {
                if (!synonyms.isEmpty()) {
                    StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(
                            settingsOption);
                    for (String singleSynonym : synonyms) {
                        aggregateFunctionSynonymGenerator.addSynonymFor(
                                singleSynonym.split(STRING_SYNONYM_DELIMITER)[0].strip(),
                                singleSynonym.split(STRING_SYNONYM_DELIMITER)[1].strip());
                    }
                    this.SETTINGS_MAP.put(settingsOption, aggregateFunctionSynonymGenerator);
                }
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
        }
        return this;
    }
}
