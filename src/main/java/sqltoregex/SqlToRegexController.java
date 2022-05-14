package sqltoregex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import sqltoregex.property.*;
import sqltoregex.property.regexgenerator.OrderRotation;
import sqltoregex.property.regexgenerator.SpellingMistake;
import sqltoregex.property.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.property.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";

    PropertyManager propertyManager;

    SqlToRegexController(PropertyManager propertyManager){
        Assert.notNull(propertyManager, "propertyManager must not be null");
        this.propertyManager = propertyManager;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(TITLE, "sql2regex");

        Property<PropertyOption> keywordSpelling = propertyManager.getPropertyByPropOption(PropertyOption.KEYWORDSPELLING, SpellingMistake.class);
        Set<PropertyOption> spellings = new HashSet<>(keywordSpelling.getSettings());
        model.addAttribute("spellings", spellings);

        Property<PropertyOption> tableNameOrder = propertyManager.getPropertyByPropOption(PropertyOption.TABLENAMEORDER, OrderRotation.class);
        Property<PropertyOption> columnNameOrder = propertyManager.getPropertyByPropOption(PropertyOption.COLUMNNAMEORDER, OrderRotation.class);
        Set<PropertyOption> orders = new HashSet<>();
        orders.addAll(tableNameOrder.getSettings());
        orders.addAll(columnNameOrder.getSettings());
        model.addAttribute("orders", orders);

        Property<SimpleDateFormat> dateFormats = propertyManager.getPropertyByPropOption(PropertyOption.DATESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateFormats", dateFormats.getSettings());

        Property<SimpleDateFormat> timeFormats = propertyManager.getPropertyByPropOption(PropertyOption.TIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("timeFormats", timeFormats.getSettings());

        Property<SimpleDateFormat> dateTimeFormats = propertyManager.getPropertyByPropOption(PropertyOption.DATETIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class);
        model.addAttribute("dateTimeFormats", dateTimeFormats.getSettings());

        Property<String> aggregateFunctionSynonyms = propertyManager.getPropertyByPropOption(PropertyOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class);
        model.addAttribute("aggregateFunctionLang", aggregateFunctionSynonyms.getSettings());

        model.addAttribute("propertyForm", new PropertyForm(spellings, orders, dateFormats.getSettings(), timeFormats.getSettings(), dateTimeFormats.getSettings(), aggregateFunctionSynonyms.getSettings(), "SELECT *"));
        model.addAttribute("activeConverter", true);
        return "home";
    }

    @PostMapping("/convert")
    public String convert(Model model, @ModelAttribute PropertyForm propertyForm){
        this.propertyManager.parseUserOptionsInput(propertyForm);
        Set<SimpleDateFormat> dateFormats = new HashSet<>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yy-MM-dd"));
        model.addAttribute("dateFormats", dateFormats);
        return "assets/propertyform";
    }

    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(TITLE, "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    @GetMapping("/visualization")
    public String visualization(Model model) {
        model.addAttribute(TITLE, "sql2regex - visualization");
        model.addAttribute("activeVisualization", true);
        return "visualization";
    }

    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(TITLE, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(TITLE, "sql2regex - privacy");
        return "privacy";
    }

    @GetMapping("/impressum")
    public String impressum(Model model) {
        model.addAttribute(TITLE, "sql2regex - impressum");
        return "impressum";
    }
}

