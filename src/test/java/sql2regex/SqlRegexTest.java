package sql2regex;

import org.junit.jupiter.api.Test;
import sql2regex.converter.SqlRegex;
import static org.assertj.core.api.Assertions.assertThat;

class SqlRegexTest {
    @Test
    void setStatementNotNull(){
        SqlRegex sqlregex = new SqlRegex("SELECT * FROM table");
        assertThat(sqlregex.getSql()).isEqualTo("SELECT * FROM table");
    }

    @Test
    void setStatement(){
        SqlRegex sqlregex = new SqlRegex();
        assertThat(sqlregex.getSql()).isEmpty();
    }

    @Test
    void setStatementNoneSetRegex(){
        SqlRegex sqlregex = new SqlRegex();
        assertThat(sqlregex.getRegex()).isEmpty();
    }
}
