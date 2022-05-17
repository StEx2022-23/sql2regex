package sqltoregex;

import net.sf.jsqlparser.expression.Expression;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sqltoregex.property.SettingsForm;
import sqltoregex.property.SettingsManager;
import sqltoregex.property.SettingsOption;
import sqltoregex.property.RegExGenerator;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

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
        this.settingsManager.parseUserOptionsInput(settingsForm);
        Set<SimpleDateFormat> dateFormats = new HashSet<>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yy-MM-dd"));
        model.addAttribute("dateFormats", dateFormats);
        return "assets/settingsform";
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

        RegExGenerator<SettingsOption, String> keywordSpelling = settingsManager.getSettingBySettingOption(
                SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        Set<SettingsOption> spellings = new HashSet<>(keywordSpelling.getSettings());
        model.addAttribute("spellings", spellings);

        RegExGenerator<SettingsOption, List<String>> tableNameOrder = settingsManager.getSettingBySettingOption(
                SettingsOption.TABLENAMEORDER, OrderRotation.class);
        RegExGenerator<SettingsOption, List<String>> columnNameOrder = settingsManager.getSettingBySettingOption(
                SettingsOption.COLUMNNAMEORDER, OrderRotation.class);
        Set<SettingsOption> orders = new HashSet<>();
        orders.addAll(tableNameOrder.getSettings());
        orders.addAll(columnNameOrder.getSettings());
        model.addAttribute("orders", orders);

        RegExGenerator<SimpleDateFormat, Expression> dateFormats = settingsManager.getSettingBySettingOption(
                SettingsOption.DATESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateFormats", dateFormats.getSettings());

        RegExGenerator<SimpleDateFormat, Expression> timeFormats = settingsManager.getSettingBySettingOption(
                SettingsOption.TIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("timeFormats", timeFormats.getSettings());

        RegExGenerator<SimpleDateFormat, Expression> dateTimeFormats = settingsManager.getSettingBySettingOption(
                SettingsOption.DATETIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateTimeFormats", dateTimeFormats.getSettings());

        RegExGenerator<String, String> aggregateFunctionSynonyms = settingsManager.getSettingBySettingOption(
                SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class);
        model.addAttribute("aggregateFunctionLang", aggregateFunctionSynonyms.getSettings());

        model.addAttribute("settingsForm",
                           new SettingsForm(spellings, orders, dateFormats.getSettings(), timeFormats.getSettings(),
                                            dateTimeFormats.getSettings(), aggregateFunctionSynonyms.getSettings(),
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

