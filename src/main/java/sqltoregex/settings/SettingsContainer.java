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

public class SettingsContainer {
    private final Map<SettingsOption, IRegExGenerator<?>> allSettings;

    private SettingsContainer(Builder builder){
        Assert.notNull(builder.unModifiableMap, "Builder must not be null!");
        this.allSettings = builder.unModifiableMap;
    }

    public IRegExGenerator<?> get(SettingsOption settingsOption){
        for (IRegExGenerator<?> generator : this.allSettings.values()){
            if (generator.getSettingsOption() == settingsOption){
                return generator;
            }
        }
        return null;
    }

    public <T extends IRegExGenerator<?>> Map<SettingsOption, T> get(Class<T> clazz){
        Map<SettingsOption, T> map = new EnumMap<>(SettingsOption.class);

        for (Map.Entry<SettingsOption,?> entry : this.allSettings.entrySet()){
            try{
                map.put(entry.getKey(), clazz.cast(entry.getValue()));
            }catch (ClassCastException e){
                //continue trying to cast other values
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public Map<SettingsOption, IRegExGenerator<?>> getAllSettings(){
        return this.allSettings;
    }


    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder {
        private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
        private static final String STRING_SYNONYM_DELIMITER = ";";
        private final Map<SettingsOption,IRegExGenerator<?>> modifiableMap = new EnumMap<>(SettingsOption.class);
        Map<SettingsOption,IRegExGenerator<?>> unModifiableMap;


        private Builder(){
        }

        public Builder with(IRegExGenerator<?> regExGenerator){
            this.modifiableMap.put(regExGenerator.getSettingsOption(), regExGenerator);
            return this;
        }

        public Builder with(SettingsContainer settingsContainer) {
            modifiableMap.putAll(settingsContainer.getAllSettings());
            return this;
        }

        public SettingsContainer with(SettingsManager settingsManager, SettingsType settingsType){
            this.with(settingsManager.getSettingsContainer(settingsType));
            return this.build();
        }

        public Builder withNodeList(NodeList nodeList, SettingsOption settingsOption) {
            if (nodeList.item(0).getTextContent().equals("false")) {
                return this;
            }

            switch (settingsOption) {
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

        public Builder withSettingsOption(SettingsOption settingsOption) {
            switch (settingsOption) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, INDEXCOLUMNNAMESPELLING -> {
                    SpellingMistake spellingMistake = new SpellingMistake(settingsOption);
                    this.modifiableMap.put(settingsOption, spellingMistake);
                }
                case TABLENAMEORDER, COLUMNNAMEORDER, INDEXCOLUMNNAMEORDER, GROUPBYELEMENTORDER -> {
                    OrderRotation orderRotation = new OrderRotation(settingsOption);
                    this.modifiableMap.put(settingsOption, orderRotation);
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
            return this;
        }

        public Builder withSettingsOptionSet(Set<SettingsOption> settingsOptions) {
            Assert.notNull(settingsOptions, "Set of settings options must not be null");
            for (SettingsOption settingsOption : settingsOptions) {
                withSettingsOption(settingsOption);
            }
            return this;
        }

        public Builder withSimpleDateFormatSet(Set<SimpleDateFormat> synonyms, SettingsOption settingsOption) {
            Assert.notNull(synonyms, "Set of simple date formats options must not be null");
            switch (settingsOption) {
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                            settingsOption);
                    for (SimpleDateFormat format : synonyms) {
                        synonymGenerator.addSynonym(format);
                    }
                    this.modifiableMap.put(settingsOption, synonymGenerator);
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
            return this;
        }

        public Builder withStringSet(Set<String> synonyms, SettingsOption settingsOption) {
            switch (settingsOption) {
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    DateAndTimeFormatSynonymGenerator synonymGenerator = new DateAndTimeFormatSynonymGenerator(
                            settingsOption);
                    for (String format : synonyms) {
                        synonymGenerator.addSynonym(new SimpleDateFormat(format));
                    }
                    this.modifiableMap.put(settingsOption, synonymGenerator);
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
                        this.modifiableMap.put(settingsOption, aggregateFunctionSynonymGenerator);
                    }
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
            return this;
        }

        public SettingsContainer build(){
            this.unModifiableMap = Collections.unmodifiableMap(this.modifiableMap);
            return new SettingsContainer(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingsContainer that)) return false;
        return allSettings.equals(that.allSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allSettings);
    }
}
