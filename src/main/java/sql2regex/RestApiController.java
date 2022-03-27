package sql2regex;

import org.springframework.web.bind.annotation.*;
import sql2regex.converter.SqlRegex;


@RestController
public class RestApiController {

    private final sql2regex.converter.SqlRegex sqlregex = new SqlRegex();

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
    public SqlRegex ConvertSql2Regex(@RequestBody String sql) {
        this.getSqlRegex().setSql(sql);
        this.getSqlRegex().convert();
        return this.getSqlRegex();
    }
}