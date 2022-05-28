package sqltoregex;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestApiController {
    @GetMapping("/convert")
    public String convertSql2RegexMulti() {
        return "API coming soon.";
    }
}