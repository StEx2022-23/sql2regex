package sqltoregex.settings;

import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The SettingsContainer hold all enabled/selected Settings.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
public class SettingsContainer {
    private final Map<SettingsOption, IRegExGenerator<?>> allSettings;

    /**
     * Constructor for the SettingsContainer, with a not null {@link Builder}.
     * @param builder SettingsContainer {@link Builder}
     */
    private SettingsContainer(Builder builder){
        Assert.notNull(builder.unModifiableMap, "Builder must not be null!");
        this.allSettings = builder.unModifiableMap;
    }

    /**
     * Returns an object which is instanceof {@link IRegExGenerator} by passing a {@link SettingsOption}.
     * @param settingsOption one of enum {@link SettingsOption}
     * @return Object instanceof {@link IRegExGenerator}
     */
    public IRegExGenerator<?> get(SettingsOption settingsOption){
        for (IRegExGenerator<?> generator : this.allSettings.values()){
            if (generator.getSettingsOption() == settingsOption){
                return generator;
            }
        }
        return null;
    }

    /**
     * Returns a map with objects which are instanceof {@link IRegExGenerator} by passing a class.
     * @param clazz Class of object which is instanceof {@link IRegExGenerator}
     * @param <T> instanceof {@link IRegExGenerator}
     * @return Map with key = {@link SettingsOption} and value = object instanceof {@link IRegExGenerator}
     */
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

    /**
     * Gets all currently saved Settings.
     * @return Map with key = {@link SettingsOption} and value = object instanceof {@link IRegExGenerator}
     */
    public Map<SettingsOption, IRegExGenerator<?>> getAllSettings(){
        return this.allSettings;
    }

    /**
     * Get a new SettingsContainer-Builder.
     * @return Builder for building SettingsContainer with method chaining
     */
    public static Builder builder(){
        return new Builder();
    }

    /**
     * Specific builder static inner class, to build the  {@link SettingsContainer}.
     */
    public static final class Builder {
        private static final String UNSUPPORTED_BUILD_WITH = "Unsupported build with:";
        private static final String STRING_SYNONYM_DELIMITER = ";";
        private final Map<SettingsOption,IRegExGenerator<?>> modifiableMap = new EnumMap<>(SettingsOption.class);
        private Map<SettingsOption,IRegExGenerator<?>> unModifiableMap;

        /**
         * Append an entry to the builder map, which holds {@link SettingsOption} and related setting objects. In this case, by passing an object instanceof {@link IRegExGenerator}.
         * @param regExGenerator object instanceof {@link IRegExGenerator}
         * @return this {@link Builder} for method chaining
         */
        public Builder with(IRegExGenerator<?> regExGenerator){
            this.modifiableMap.put(regExGenerator.getSettingsOption(), regExGenerator);
            return this;
        }

        /**
         * Append an or multiple entry to the builder map, which holds {@link SettingsOption} and related setting objects. In this case, by passing  {@link SettingsContainer}.
         * @param settingsContainer  {@link SettingsContainer}
         * @return this {@link Builder} for method chaining
         */
        public Builder with(SettingsContainer settingsContainer) {
            modifiableMap.putAll(settingsContainer.getAllSettings());
            return this;
        }

        /**
         * Append an or multiple entry to the builder map, which holds {@link SettingsOption} and related setting objects. In this case, by passing the {@link SettingsManager} and one of enum {@link SettingsType}.
         * @param settingsManager {@link SettingsManager}
         * @param settingsType one of enum {@link SettingsType}
         * @return this {@link Builder} for method chaining
         */
        public SettingsContainer with(SettingsManager settingsManager, SettingsType settingsType){
            this.with(settingsManager.getSettingsContainer(settingsType));
            return this.build();
        }

        /**
         * Append an entry to the builder map, which holds {@link SettingsOption} and related setting objects. In this case, by passing a {@link NodeList} and one of enum {@link SettingsOption}.
         * @param nodeList NodeList from the xml parsing process
         * @param settingsOption one of enum {@link SettingsOption}
         * @return this {@link Builder} for method chaining
         */
        public Builder withNodeList(NodeList nodeList, SettingsOption settingsOption) {
            if (nodeList.item(0).getTextContent().equals("false")) {
                return this;
            }

            switch (settingsOption) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, INDEXCOLUMNNAMESPELLING, STRINGVALUESPELLING, TABLENAMEORDER, COLUMNNAMEORDER, GROUPBYELEMENTORDER, INDEXCOLUMNNAMEORDER, INSERTINTOVALUESORDER -> this.with(
                        settingsOption);
                case DATESYNONYMS, TIMESYNONYMS, DATETIMESYNONYMS -> {
                    Set<String> valueList = new HashSet<>();
                    SettingsNodeListIterator settingsNodeListIterator = new SettingsNodeListIterator(nodeList);
                    for (Node node : settingsNodeListIterator) {
                        valueList.add(node.getTextContent());
                    }
                    this.withStringSet(valueList, settingsOption);
                }
                case AGGREGATEFUNCTIONLANG, DATATYPESYNONYMS, OTHERSYNONYMS -> {
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

        /**
         * Creates a {@link IRegExGenerator} depending on the provided {@link SettingsOption}.
         * @param settingsOption for creating related {@link sqltoregex.settings.regexgenerator.RegExGenerator}
         * @return this {@link Builder} for method chaining
         */
        public Builder with(SettingsOption settingsOption) {
            switch (settingsOption) {
                case KEYWORDSPELLING, TABLENAMESPELLING, COLUMNNAMESPELLING, INDEXCOLUMNNAMESPELLING, STRINGVALUESPELLING -> {
                    SpellingMistake spellingMistake = new SpellingMistake(settingsOption);
                    this.modifiableMap.put(settingsOption, spellingMistake);
                }
                case TABLENAMEORDER, COLUMNNAMEORDER, INDEXCOLUMNNAMEORDER, GROUPBYELEMENTORDER, INSERTINTOVALUESORDER -> {
                    OrderRotation orderRotation = new OrderRotation(settingsOption);
                    this.modifiableMap.put(settingsOption, orderRotation);
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
            return this;
        }

        /**
         * Creates a {@link IRegExGenerator} depending on the provided Set of {@link SettingsOption}s.
         * Therefore, hands calls over to {@link Builder#with(SettingsOption)}.
         * @param settingsOptions Set of {@link SettingsOption}s
         * @return this {@link Builder} for method chaining
         */
        public Builder withSettingsOptionSet(Set<SettingsOption> settingsOptions) {
            Assert.notNull(settingsOptions, "Set of settings options must not be null");
            for (SettingsOption settingsOption : settingsOptions) {
                with(settingsOption);
            }
            return this;
        }

        /**
         * Creates a {@link IRegExGenerator} depending on the provided set of {@link SimpleDateFormat}s and {@link SettingsOption}.
         * @param synonyms synonyms set as SimpleDateFormats which will be added to the {@link IRegExGenerator}
         * @param settingsOption {@link SettingsOption} for determine which {@link IRegExGenerator} will be created
         * @return this {@link Builder} for method chaining
         */
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

        /**
         * Creates a {@link IRegExGenerator} depending on the provided {@link SettingsOption} and synonym Set.
         * If the synonymSet consists of Strings delimited by {@link Builder#STRING_SYNONYM_DELIMITER} they are split
         * and added with {@link sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator#addSynonymFor(Object, Object)}
         * @param synonyms synonym set as set of string
         * @param settingsOption {@link SettingsOption}
         * @return this {@link Builder} for method chaining
         */
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
                case AGGREGATEFUNCTIONLANG, DATATYPESYNONYMS, OTHERSYNONYMS -> {
                    if (synonyms != null && !synonyms.isEmpty()) {
                        StringSynonymGenerator aggregateFunctionSynonymGenerator = new StringSynonymGenerator(
                                settingsOption);
                        for (String singleSynonym : synonyms) {
                            if (singleSynonym.split(STRING_SYNONYM_DELIMITER).length == 1){
                                aggregateFunctionSynonymGenerator.addSynonym(singleSynonym);
                            }else {
                                aggregateFunctionSynonymGenerator.addSynonymFor(
                                        singleSynonym.split(STRING_SYNONYM_DELIMITER)[0].strip(),
                                        singleSynonym.split(STRING_SYNONYM_DELIMITER)[1].strip());
                            }
                        }
                        this.modifiableMap.put(settingsOption, aggregateFunctionSynonymGenerator);
                    }
                }
                default -> throw new IllegalArgumentException(UNSUPPORTED_BUILD_WITH + settingsOption);
            }
            return this;
        }

        /**
         * {@return the finally constructed {@link SettingsContainer} with corresponding settings}.
         */
        public SettingsContainer build(){
            this.unModifiableMap = Collections.unmodifiableMap(this.modifiableMap);
            return new SettingsContainer(this);
        }


        /**
         * Pushes all provided {@link IRegExGenerator} into the {@link SettingsOption}s map.
         * @param generatorSet Set of {@link IRegExGenerator}s which will be added.
         * @return this {@link Builder} for method chaining
         */
        public Builder withRegExGeneratorSet(Set<? extends IRegExGenerator<?>> generatorSet) {
            Assert.notNull(generatorSet, "Set of generators must not be null");
            for (IRegExGenerator<?> generator : generatorSet) {
                this.modifiableMap.put(generator.getSettingsOption(), generator);
            }
            return this;
        }

    }

    /**
     * Overrides default equals method. Objects are equal, if the allSettings map is equal.
     * @param o to compare object
     * @return boolean if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingsContainer that)) return false;
        return allSettings.equals(that.allSettings);
    }

    /**
     * Overrides default hashCode() method. HashCode candidate is the allSettings map.
     * @return hashcode as int
     */
    @Override
    public int hashCode() {
        return Objects.hash(allSettings);
    }
}
