package sqltoregex;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RestApiController {
    @GetMapping("/convert")
    public String convertSql2RegexMulti() {
        return "API coming soon.";
    }
}