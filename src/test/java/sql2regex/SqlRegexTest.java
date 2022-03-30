package sql2regex;

import org.junit.jupiter.api.Test;
import sql2regex.converter.SqlRegex;
import static org.junit.jupiter.api.Assertions.*;

class SqlRegexTest {
    @Test
    void setStatementNotNull(){
        SqlRegex sqlregex = new SqlRegex("SELECT * FROM table");
        assertEquals("SELECT * FROM table", sqlregex.getSql());
    }

    @Test
    void initSqlRegexGetSql(){
        SqlRegex sqlregex = new SqlRegex();
        assertTrue(sqlregex.getSql().isEmpty());
    }

    @Test
    void setEmptySQL(){
        SqlRegex sqlregex = new SqlRegex();
        Exception exception = assertThrows(NullPointerException.class, () -> sqlregex.setSql(""));
        assertEquals("SQL-Input-String should have more characters than null.", exception.getMessage());
    }

    @Test
    void initSqlRegexGetRegex(){
        SqlRegex sqlregex = new SqlRegex();
        assertTrue(sqlregex.getRegex().isEmpty());
    }
}
