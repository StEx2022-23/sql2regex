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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides frontend pages for this application.
 */
@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";
    private final SettingsManager settingsManager;
    private final ConverterManagement converterManagement;

    /**
     * Sets SettingsManager and ConverterManagement for the converting process.
     * @param settingsManager SettingsManager, autowired, no action required.
     * @param converterManagement ConverterManagement, autowired, no action required.
     */
    @Autowired
    SqlToRegexController(SettingsManager settingsManager, ConverterManagement converterManagement) {
        Assert.notNull(settingsManager, "Settings manager must not be null");
        Assert.notNull(converterManagement, "Converter management must not be null");
        this.settingsManager = settingsManager;
        this.converterManagement = converterManagement;
    }

    /**
     * Prepares and returns the about page when requesting "/about".
     * @param model Model, autowired, no action required.
     * @return about page
     */
    @GetMapping("/about")
    public String aboutUs(Model model) {
        model.addAttribute(TITLE, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    /**
     * Prepares settings for displaying on frontend.
     * @param model Model, autowired, no action required.
     * @return SettingsForm, html asset
     */
    private SettingsForm addSettingsFormFields(Model model) {
        model.addAttribute("spellings", settingsManager.getSettingByClass(SpellingMistake.class, SettingsType.ALL));
        model.addAttribute("orders", settingsManager.getSettingByClass(OrderRotation.class, SettingsType.ALL));
        //Grouping all elements by the delimiter of the parts of the format for frontend convenience
        model.addAttribute("dateFormats",getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATESYNONYMS,
                                                         SettingsType.ALL).stream().collect(Collectors.groupingBy(el -> {
            String patternString = ((SimpleDateFormat) el).toPattern();
            Pattern pattern = Pattern.compile("[^a-zA-Z]");
            Matcher matcher = pattern.matcher(patternString);

            if (matcher.find()) {
                return patternString.charAt(matcher.start());
            } else {
                return " ";
            }
        })));
        model.addAttribute("timeFormats",
                           getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class, SettingsOption.TIMESYNONYMS,
                                           SettingsType.ALL));
        model.addAttribute("dateTimeFormats", getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class,
                                                              SettingsOption.DATETIMESYNONYMS, SettingsType.ALL));
        //special preprocessing to render comfortably on frontend
        Map<String, Set<String>> aggregateFunctionSynonymsMap = settingsManager.getSettingBySettingsOption(
                        SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("aggregateFunctionLang", aggregateFunctionSynonymsMap);
        Map<String, Set<String>> functionLang = settingsManager.getSettingBySettingsOption(
                        SettingsOption.FUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("functionLang", functionLang);
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
                settingsManager.getSettingByClass(SpellingMistake.class, SettingsType.DEFAULT_SCHOOL),
                settingsManager.getSettingByClass(OrderRotation.class, SettingsType.DEFAULT_SCHOOL),
                getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATESYNONYMS,
                                SettingsType.DEFAULT_SCHOOL),
                getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class, SettingsOption.TIMESYNONYMS,
                                SettingsType.DEFAULT_SCHOOL),
                getSynonymSetOf(DateAndTimeFormatSynonymGenerator.class, SettingsOption.DATETIMESYNONYMS,
                                SettingsType.DEFAULT_SCHOOL),
                //special preprocessing to render comfortably on frontend
                settingsManager.getSettingBySettingsOption(
                                SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
                        .map(synonymGenerator -> GraphPreProcessor.getSynonymSetWithDelimiter(synonymGenerator.getGraph(), ";"))
                        .orElse(new HashSet<>()),
                settingsManager.getSettingBySettingsOption(
                                SettingsOption.FUNCTIONLANG, StringSynonymGenerator.class, SettingsType.ALL)
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
     * Performes converting process by a post request.
     * @param model Model, autowired, no action required.
     * @param settingsForm SettingsForm
     * @param result Errors
     * @return filled form
     * @throws JSQLParserException if parsing goes wrong
     */
    @PostMapping("/convert")
    public String convert(Model model, @Valid @ModelAttribute SettingsForm settingsForm,
                          Errors result) throws JSQLParserException {
        addSettingsFormFields(model);
        if (result.hasErrors()) return "assets/settingsform/form";
        this.settingsManager.parseUserSettingsInput(settingsForm);

        model.addAttribute("settingsForm",
                new SettingsForm(
                        settingsForm.getSpellings(),
                        settingsForm.getOrders(),
                        settingsForm.getDateFormats(),
                        settingsForm.getTimeFormats(),
                        settingsForm.getDateTimeFormats(),
                        settingsForm.getAggregateFunctionLang(),
                        settingsForm.getFunctionLang(),
                        settingsForm.getDatatypeSynonyms(),
                        settingsForm.getOtherSynonyms(),
                        settingsForm.getSql()
                )
        );

        model.addAttribute("regex", converterManagement.deparse(settingsForm.getSql()));

        return "assets/settingsform/form";
    }

    /**
     * Prepares and returns the examples page when requesting "/examples".
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
     * Helper function to return all synonym generators.
     * @param clazz Defines class of the specific synonym generator.
     * @param settingsOption SettingsOption to define a specific synonym generator.
     * @param settingsType SettingsType defines presets.
     * @return Set of synonym generators
     */
    private <T, S> Set<T> getSynonymSetOf(Class<? extends SynonymGenerator<T, S>> clazz,
                                          SettingsOption settingsOption, SettingsType settingsType) {
        return settingsManager.getSettingBySettingsOption(settingsOption, clazz, settingsType)
                .map(generator -> GraphPreProcessor.sortSet(GraphPreProcessor.getSynonymSet(generator.getGraph())))
                .orElse(new LinkedHashSet<>());
    }

    /**
     * Prepares and returns the landing page when requesting "/".
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
     * Prepares and returns the landing page when requesting "/".
     * @param model Model, autowired, no action required.
     * @return landing page
     */
    @GetMapping("/howto")
    public String howTo(Model model) {
        model.addAttribute(TITLE, "sql2regex - How-To");
        model.addAttribute("activeHowTo", true);
        return "howTo";
    }

    /**
     * Prepares and returns the legal notice page when requesting "/legal".
     * @param model Model, autowired, no action required.
     * @return legal notice page
     */
    @GetMapping("/legal")
    public String legal(Model model) {
        model.addAttribute(TITLE, "sql2regex - legal notice");
        return "legalNotice";
    }

    /**
     * Prepares and returns the privacy policy page when requesting "/privacy".
     * @param model Model, autowired, no action required.
     * @return privacy policy page
     */
    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(TITLE, "sql2regex - privacy");
        return "privacy";
    }

    /**
     * Prepares and returns the visualization page when requesting "/visualization".
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