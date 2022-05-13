package sqltoregex;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class RestApiController {
    @PostMapping("/convert")
    public String convertSql2RegexMulti() {
        return "API coming soon.";
    }
}