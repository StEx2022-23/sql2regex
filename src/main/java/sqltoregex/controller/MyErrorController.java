package sqltoregex.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * The controller for error handling on the frontend page.
 */
@Controller
public class MyErrorController implements ErrorController {
    /**
     * Prints funny error pages, with cats, if a 4xx error occures.
     * @param request HttpServletRequest, autowired, no action required
     * @param model Model, autowired, no action required
     * @return ErrorPage
     */
    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String imgSRC = "https://http.cat/" + status.toString() + ".jpg";
        model.addAttribute("imgsrc", imgSRC);
        model.addAttribute("title", "sql2regex - ERROR");
        return "error";
    }
}