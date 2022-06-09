package sqltoregex;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Api-Controller for Converting.
 */
@RestController
@RequestMapping("/api")
public class RestApiController {
    /**
     * Handle rest api requests on "/api".
     * @return json with converting results
     */
    @GetMapping("/convert")
    public String apiConvertSingleStatement() {
        return "API coming soon.";
    }
}