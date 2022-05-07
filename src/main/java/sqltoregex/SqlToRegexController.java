package sqltoregex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SqlToRegexController {
    private final String title = "title";

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(this.title, "sqltoregex");
        model.addAttribute("activeConverter", true);
        return "home";
    }

    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(this.title, "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(this.title, "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(this.title, "sql2regex - privacy");
        return "privacy";
    }

    @GetMapping("/impressum")
    public String impressum(Model model) {
        model.addAttribute(this.title, "sql2regex - impressum");
        return "impressum";
    }
}

