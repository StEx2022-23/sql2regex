package sql2regex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    private String pageTitelAttributeName = "title";

    public String getPageTitelAttributeName(){
        return this.pageTitelAttributeName;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(this.getPageTitelAttributeName(), "sql2regex");
        model.addAttribute("activeConverter", true);
        return "home";
    }

    @GetMapping("/examples")
    public String examples(Model model) {
        model.addAttribute(this.getPageTitelAttributeName(), "sql2regex - examples");
        model.addAttribute("activeExamples", true);
        return "examples";
    }

    @GetMapping("/about")
    public String aboutus(Model model) {
        model.addAttribute(this.getPageTitelAttributeName(), "sql2regex - about us");
        model.addAttribute("activeAbout", true);
        return "about";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute(this.getPageTitelAttributeName(), "sql2regex - privacy");
        return "privacy";
    }

    @GetMapping("/impressum")
    public String impressum(Model model) {
        model.addAttribute(this.getPageTitelAttributeName(), "sql2regex - impressum");
        return "impressum";
    }
}

