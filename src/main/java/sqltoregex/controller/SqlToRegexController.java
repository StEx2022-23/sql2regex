package sqltoregex.controller;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.xml.sax.SAXException;
import sqltoregex.ConverterManagement;
import sqltoregex.settings.SettingsForm;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.ExpressionRotation;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";
    private static final boolean USE_DEFAULT = true;

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

    @PostMapping("/convert")
    public String convert(Model model, @Valid @ModelAttribute SettingsForm settingsForm, Errors result) throws XPathExpressionException, JSQLParserException, ParserConfigurationException, IOException, SAXException {
        addSettingsFormFields(model);
        
        if(result.hasErrors()) {
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

        model.addAttribute("regex",  converterManagement.deparse(settingsForm.getSql()));

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

    private SettingsForm addSettingsFormFields(Model model){
        Set<SettingsOption> spellings = new HashSet<>();
        settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class, USE_DEFAULT)
                .ifPresent(keywordSpelling -> spellings.add(keywordSpelling.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMESPELLING, SpellingMistake.class, USE_DEFAULT)
                .ifPresent(columnNameSpelling -> spellings.add(columnNameSpelling.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMESPELLING, SpellingMistake.class, USE_DEFAULT)
                .ifPresent(tableNameSpelling -> spellings.add(tableNameSpelling.getSettingsOption()));
        model.addAttribute("spellings", spellings);

        Set<SettingsOption> orders = new HashSet<>();
        settingsManager.getSettingBySettingsOption(SettingsOption.TABLENAMEORDER, OrderRotation.class, USE_DEFAULT)
                .ifPresent(tableNameOrder -> orders.add(tableNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.COLUMNNAMEORDER, OrderRotation.class, USE_DEFAULT)
                .ifPresent(columnNameOrder -> orders.add(columnNameOrder.getSettingsOption()));
        settingsManager.getSettingBySettingsOption(SettingsOption.EXPRESSIONORDER, ExpressionRotation.class, USE_DEFAULT)
                .ifPresent(expressionOrder -> orders.add(expressionOrder.getSettingsOption()));
        model.addAttribute("orders", orders);

        Set<SimpleDateFormat> dateFormats = settingsManager.getSynonymManagerBySettingOption(SettingsOption.DATESYNONYMS, DateAndTimeFormatSynonymGenerator.class, USE_DEFAULT)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymSet(synonymGenerator.getGraph()))
                .orElse(new LinkedHashSet<>());
        model.addAttribute("dateFormats", dateFormats);

        Set<SimpleDateFormat> timeFormats = settingsManager.getSynonymManagerBySettingOption(SettingsOption.TIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class, USE_DEFAULT)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymSet(synonymGenerator.getGraph()))
                .orElse(new LinkedHashSet<>());
        model.addAttribute("timeFormats", timeFormats);

        Set<SimpleDateFormat> dateTimeFormats = settingsManager.getSynonymManagerBySettingOption(SettingsOption.DATETIMESYNONYMS, DateAndTimeFormatSynonymGenerator.class, USE_DEFAULT)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymSet(synonymGenerator.getGraph()))
                .orElse(new LinkedHashSet<>());
        model.addAttribute("dateTimeFormats",dateTimeFormats);

        Set<String> aggregateFunctionSynonymsSet = settingsManager.getSynonymManagerBySettingOption(SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, USE_DEFAULT)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymSet(synonymGenerator.getGraph()))
                .orElse(new LinkedHashSet<>());
        Map<String, Set<String>> aggregateFunctionSynonymsMap = settingsManager.getSynonymManagerBySettingOption(SettingsOption.AGGREGATEFUNCTIONLANG, StringSynonymGenerator.class, USE_DEFAULT)
                .map(synonymGenerator -> GraphPreProcessor.getSynonymMap(synonymGenerator.getGraph()))
                .orElse(new HashMap<>());
        model.addAttribute("aggregateFunctionLang", aggregateFunctionSynonymsMap);

        model.addAttribute("activeConverter", true);

        return new SettingsForm(
                spellings,
                orders,
                dateFormats,
                timeFormats,
                dateTimeFormats,
                aggregateFunctionSynonymsSet,
                ""
        );
    }
}

