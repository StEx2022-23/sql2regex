package sqltoregex.controller;

import net.sf.jsqlparser.JSQLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sqltoregex.ConverterManagement;
import sqltoregex.settings.SettingsForm;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import javax.validation.Valid;
import java.util.*;

/**
 * Handles frontend pages for this applications.
 */
@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";

    SettingsManager settingsManager;
    ConverterManagement converterManagement;

    /**
     * Set SettingsManager and ConverterManagement for the converting process.
     * @param settingsManager SettingsManager, autowired, no action required.
     * @param converterManagement ConverterManagement, autowired, no action required.
     */
    @Autowired
    SqlToRegexController(SettingsManager settingsManager, ConverterManagement converterManagement) {
        Assert.notNull(settingsManager, "Settings management must not be null");
        Assert.notNull(converterManagement, "Converter management must not be null");
        this.settingsManager = settingsManager;
        this.converterManagement = converterManagement;
    }

    /**
     * Prepare and return the about page when requesting "/about".
     * @param model Model, autowired, no action required.
     * @return about page
     */
    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(TITLE, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    /**
     * Prepare settings for displaying on frontend.
     * @param model Model, autowired, no action required.
     * @return SettingsForm, html asset
     */
    private SettingsForm addSettingsFormFields(Model model) {
        model.addAttribute("spellings", this.getSpellings(SettingsType.ALL));
        model.addAttribute("orders", this.getOrders(SettingsType.ALL));
        model.addAttribute("dateFormats",
                getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATESYNONYMS,
                        SettingsType.ALL));
        model.addAttribute("timeFormats",
                getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class, SettingsOption.TIMESYNONYMS,
                        SettingsType.ALL));
        model.addAttribute("dateTimeFormats", getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class,
                SettingsOption.DATETIMESYNONYMS, SettingsType.ALL));
        //special preprocessing to render comfortably on frontend
        Map<String, Set<String>> aggregateFunctionSynonymsMap = settingsManager.getSettingBySettingsOption(
                        SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("aggregateFunctionLang", aggregateFunctionSynonymsMap);
        Map<String, Set<String>> datatypeSynonymsMap = settingsManager.getSettingBySettingsOption(
                        SettingsOption.DATATYPESYNONYMS, StringSynonymGenerator.class, SettingsType.ALL)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("datatypeSynonyms", datatypeSynonymsMap);
        Map<String, Set<String>> otherSynonymsMap = settingsManager.getSettingBySettingsOption(
                        SettingsOption.OTHERSYNONYMS, StringSynonymGenerator.class, SettingsType.ALL)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("otherSynonyms", otherSynonymsMap);


        model.addAttribute("activeConverter", true);

        //the SettingsType could be later read in by a RequestParameter to provide several predefined default settings
        return new SettingsForm(
                this.getSpellings(SettingsType.DEFAULT_SCHOOL),
                this.getOrders(SettingsType.DEFAULT_SCHOOL),
                getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATESYNONYMS,
                        SettingsType.DEFAULT_SCHOOL),
                getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class, SettingsOption.TIMESYNONYMS,
                        SettingsType.DEFAULT_SCHOOL),
                getSynonymGenerators(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATETIMESYNONYMS,
                                     SettingsType.DEFAULT_SCHOOL),
                //special preprocessing to render comfortably on frontend
                settingsManager.getSettingBySettingsOption(
                                SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                        .map(synonymGenerator -> GraphPreProcessor.getSynonymSetWithDelimiter(synonymGenerator.getGraph(), ";"))
                        .orElse(new HashSet<>()),
                settingsManager.getSettingBySettingsOption(
                                SettingsOption.DATATYPESYNONYMS, StringSynonymGenerator.class, SettingsType.ALL)
                        .map(synonymGenerator -> GraphPreProcessor.getSynonymSetWithDelimiter(synonymGenerator.getGraph(), ";"))
                        .orElse(new HashSet<>()),
                settingsManager.getSettingBySettingsOption(
                                SettingsOption.OTHERSYNONYMS, StringSynonymGenerator.class, SettingsType.ALL)
                        .map(synonymGenerator -> GraphPreProcessor.getSynonymSetWithDelimiter(synonymGenerator.getGraph(), ";"))
                        .orElse(new HashSet<>()),
                ""
        );
    }

    /**
     * Performe converting Process by post request.
     * @param model Model, autowired, no action required.
     * @param settingsForm SettingsForm
     * @param result Errors
     * @return filled form
     * @throws JSQLParserException
     */
    @PostMapping("/convert")
    public String convert(Model model, @Valid @ModelAttribute SettingsForm settingsForm,
                          Errors result) throws JSQLParserException {
        addSettingsFormFields(model);

        if (result.hasErrors()) {
            return "assets/settingsform/form";
        }

        this.settingsManager.parseUserSettingsInput(settingsForm);

        model.addAttribute("settingsForm",
                new SettingsForm(
                        settingsForm.getSpellings(),
                        settingsForm.getOrders(),
                        settingsForm.getDateFormats(),
                        settingsForm.getTimeFormats(),
                        settingsForm.getDateTimeFormats(),
                        settingsForm.getAggregateFunctionLang(),
                        settingsForm.getDatatypeSynonyms(),
                        settingsForm.getOtherSynonyms(),
                        settingsForm.getSql()
                )
        );

        model.addAttribute("regex", converterManagement.deparse(settingsForm.getSql()));

        return "assets/settingsform/form";
    }

    /**
     * Prepare and return the examples page when requesting "/examples".
     * @param model Model, autowired, no action required.
     * @return examples page
     */
    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(TITLE, "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    /**
     * Helper function to return all order settings.
     * @param settingsType SettingsType defines presets.
     * @return Set of SettingsOption
     */
    private Set<SettingsOption> getOrders(SettingsType settingsType) {
        Set<SettingsOption> orders = new HashSet<>();
        settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER, OrderRotation.class, settingsType)
                .ifPresent(tableNameOrder -> orders.add(tableNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class, settingsType)
                .ifPresent(columnNameOrder -> orders.add(columnNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.GROUPBYELEMENTORDER, OrderRotation.class,
                        settingsType)
                .ifPresent(expressionOrder -> orders.add(expressionOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.INSERTINTOVALUESORDER, OrderRotation.class,
                        settingsType)
                .ifPresent(expressionOrder -> orders.add(expressionOrder.getSettingsOption()));
        return orders;
    }

    /**
     * Helper function to return all spelling settings.
     * @param settingsType SettingsType defines presets.
     * @return Set of SettingsOption
     */
    private Set<SettingsOption> getSpellings(SettingsType settingsType) {
        Set<SettingsOption> spellings = new HashSet<>();
        settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class, settingsType)
                .ifPresent(keywordSpelling -> spellings.add(keywordSpelling.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMESPELLING, SpellingMistake.class,
                        settingsType)
                .ifPresent(columnNameSpelling -> spellings.add(columnNameSpelling.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING, SpellingMistake.class,
                        settingsType)
                .ifPresent(tableNameSpelling -> spellings.add(tableNameSpelling.getSettingsOption()));
        return spellings;
    }

    /**
     * Helper function to return all synonym generators.
     * @param synonymGenerator Defines class of the specific synonym generator.
     * @param settingsOption SettingsOption to define a specific synonym generator.
     * @param settingsType SettingsType defines presets.
     * @return Set of synonym generators
     */
    private <T, S> Set<T> getSynonymGenerators(Class<? extends SynonymGenerator<T, S>> synonymGenerator,
                                               SettingsOption settingsOption, SettingsType settingsType) {
        return settingsManager.getSettingBySettingsOption(settingsOption, synonymGenerator, settingsType)
                .map(generator -> GraphPreProcessor.getSynonymSet(generator.getGraph()))
                .orElse(new LinkedHashSet<>());
    }

    /**
     * Prepare and return the landing page when requesting "/".
     * @param model Model, autowired, no action required.
     * @return landing page
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(TITLE, "sql2regex");
        model.addAttribute("settingsForm", addSettingsFormFields(model));
        model.addAttribute("activeConverter", true);
        return "home";
    }

    /**
     * Prepare and return the impressum page when requesting "/impressum".
     * @param model Model, autowired, no action required.
     * @return impressum page
     */
    @GetMapping("/impressum")
    public String impressum(Model model) {
        model.addAttribute(TITLE, "sql2regex - impressum");
        return "impressum";
    }

    /**
     * Prepare and return the privacy policy page when requesting "/privacy".
     * @param model Model, autowired, no action required.
     * @return privacy policy page
     */
    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(TITLE, "sql2regex - privacy");
        return "privacy";
    }

    /**
     * Prepare and return the visualization page when requesting "/visualization".
     * @param model Model, autowired, no action required.
     * @return visualization page
     */
    @GetMapping("/visualization")
    public String visualization(Model model) {
        model.addAttribute(TITLE, "sql2regex - visualization");
        model.addAttribute("activeVisualization", true);
        return "visualization";
    }
}