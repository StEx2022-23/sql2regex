package sql2regex;

import org.springframework.web.bind.annotation.*;
import sql2regex.converter.SqlRegex;
import sql2regex.converter.MultiSqlRegex;
import java.util.List;

@RestController
@RequestMapping("/")
public class RestApiController {

    private final SqlRegex sqlregex = new SqlRegex();

    public SqlRegex getSqlRegex() {
        return sqlregex;
    }

    @GetMapping("/convert")
    public String ReturnGivenParam(@RequestParam(value = "sql", defaultValue = "Pass your SQL-Statement!") String sql) {
        this.getSqlRegex().setSql(sql);
        this.getSqlRegex().convert();
        return this.getSqlRegex().toString();
    }

    @PostMapping("/convert")
    public String ConvertSql2Regex(@RequestBody SqlRegex sqlregex) {
        sqlregex.convert();
        return sqlregex.toString();
    }

    @PostMapping("/multiconvert")
    public String ConvertSql2RegexMulti(@RequestBody SqlRegex[] sqlregexlist) {
        MultiSqlRegex sqlregexmultargs = new MultiSqlRegex(List.of(sqlregexlist));
        sqlregexmultargs.convert();
        return sqlregexmultargs.toString();
    }
}