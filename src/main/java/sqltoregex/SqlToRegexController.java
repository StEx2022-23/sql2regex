package sqltoregex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sqltoregex.property.PropertyForm;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Controller
public class SqlToRegexController {
    private static final String TITLE = "title";

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(TITLE, "sql2regex");
        Set<SimpleDateFormat> dateFormats = new HashSet<SimpleDateFormat>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yy-MM-dd"));
        model.addAttribute("propertyForm", new PropertyForm(dateFormats, "SELECT *"));
        model.addAttribute("activeConverter", true);
        return "home";
    }

    @PostMapping("/convert")
    public String convert(Model model, @ModelAttribute PropertyForm propertyForm){
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

