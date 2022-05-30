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
import sqltoregex.settings.regexgenerator.GroupByElementRotation;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import javax.validation.Valid;
import java.util.*;

@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";

    SettingsManager settingsManager;
    ConverterManagement converterManagement;

    @Autowired
    SqlToRegexController(SettingsManager settingsManager, ConverterManagement converterManagement) {
        Assert.notNull(settingsManager, "Settings management must not be null");
        Assert.notNull(converterManagement, "Converter management must not be null");
        this.settingsManager = settingsManager;
        this.converterManagement = converterManagement;
    }

    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(TITLE, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

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
                getSynonymGenerators(StringSynonymGenerator.class, SettingsOption.AGGREGATEFUNCTIONLANG,
                        SettingsType.DEFAULT_SCHOOL),
                ""
        );
    }

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
                        settingsForm.getSql()
                )
        );

        model.addAttribute("regex", converterManagement.deparse(settingsForm.getSql()));

        return "assets/settingsform/form";
    }

    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(TITLE, "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    private Set<SettingsOption> getOrders(SettingsType settingsType) {
        Set<SettingsOption> orders = new HashSet<>();
        settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER, OrderRotation.class, settingsType)
                .ifPresent(tableNameOrder -> orders.add(tableNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class, settingsType)
                .ifPresent(columnNameOrder -> orders.add(columnNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.GROUPBYELEMENTORDER, GroupByElementRotation.class,
                        settingsType)
                .ifPresent(expressionOrder -> orders.add(expressionOrder.getSettingsOption()));
        return orders;
    }

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

    private <T, S> Set<T> getSynonymGenerators(Class<? extends SynonymGenerator<T, S>> synonymGenerator,
                                               SettingsOption settingsOption, SettingsType settingsType) {
        return settingsManager.getSettingBySettingsOption(settingsOption, synonymGenerator, settingsType)
                .map(generator -> GraphPreProcessor.getSynonymSet(generator.getGraph()))
                .orElse(new LinkedHashSet<>());
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(TITLE, "sql2regex");
        model.addAttribute("settingsForm", addSettingsFormFields(model));
        model.addAttribute("activeConverter", true);
        return "home";
    }

    @GetMapping("/impressum")
    public String impressum(Model model) {
        model.addAttribute(TITLE, "sql2regex - impressum");
        return "impressum";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(TITLE, "sql2regex - privacy");
        return "privacy";
    }

    @GetMapping("/visualization")
    public String visualization(Model model) {
        model.addAttribute(TITLE, "sql2regex - visualization");
        model.addAttribute("activeVisualization", true);
        return "visualization";
    }
}