package sqltoregex.controller;

import net.sf.jsqlparser.expression.Expression;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import sqltoregex.settings.SettingsForm;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";

    SettingsManager settingsManager;

    SqlToRegexController(SettingsManager settingsManager) {
        Assert.notNull(settingsManager, "Settings manager must not be null");
        this.settingsManager = settingsManager;
    }

    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(TITLE, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    @PostMapping("/convert")
    public String convert(Model model, @ModelAttribute SettingsForm settingsForm) {
        this.settingsManager.parseUserSettingsInput(settingsForm);
        Set<SimpleDateFormat> dateFormats = new HashSet<>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yy-MM-dd"));
        model.addAttribute("dateFormats", dateFormats);
        return "assets/settingsform/form";
    }

    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(TITLE, "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(TITLE, "sql2regex");

        RegExGenerator<String> keywordSpelling = settingsManager.getSettingBySettingOption(
                SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        Set<SettingsOption> spellings = new HashSet<>();
        spellings.add(keywordSpelling.getSettingsOption());
        model.addAttribute("spellings", spellings);

        RegExGenerator<List<String>> tableNameOrder = settingsManager.getSettingBySettingOption(
                SettingsOption.TABLENAMEORDER, OrderRotation.class);
        RegExGenerator<List<String>> columnNameOrder = settingsManager.getSettingBySettingOption(
                SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
        Set<SettingsOption> orders = new HashSet<>();
        orders.add(tableNameOrder.getSettingsOption());
        orders.add(columnNameOrder.getSettingsOption());
        model.addAttribute("orders", orders);

        SynonymGenerator<SimpleDateFormat, Expression> dateFormats = settingsManager.getSynonymManagerBySettingOption(
                SettingsOption.DATESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateFormats", GraphPreProcessor.getSynonymSet(dateFormats.getGraph()));

        SynonymGenerator<SimpleDateFormat, Expression> timeFormats = settingsManager.getSynonymManagerBySettingOption(
                SettingsOption.TIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("timeFormats", GraphPreProcessor.getSynonymSet(dateFormats.getGraph()));

        SynonymGenerator<SimpleDateFormat, Expression> dateTimeFormats = settingsManager.getSynonymManagerBySettingOption(
                SettingsOption.DATETIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateTimeFormats", GraphPreProcessor.getSynonymSet(dateFormats.getGraph()));

        SynonymGenerator<String, String> aggregateFunctionSynonyms = settingsManager.getSynonymManagerBySettingOption(
                SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class);
        model.addAttribute("aggregateFunctionLang", GraphPreProcessor.getSynonymMap(aggregateFunctionSynonyms.getGraph()));

        model.addAttribute("settingsForm",
                           new SettingsForm(spellings, orders, GraphPreProcessor.getSynonymSet(dateFormats.getGraph()), GraphPreProcessor.getSynonymSet(timeFormats.getGraph()),
                                            GraphPreProcessor.getSynonymSet(dateTimeFormats.getGraph()), GraphPreProcessor.getSynonymSet(aggregateFunctionSynonyms.getGraph()),
                                            "SELECT *"));
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

