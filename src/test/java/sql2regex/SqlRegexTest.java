package sql2regex;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;
import sql2regex.converter.SqlRegex;

import static org.junit.jupiter.api.Assertions.*;

class SqlRegexTest {
    @Test
    void setStatementNotNull(){
        SqlRegex sqlregex = new SqlRegex("SELECT * FROM table");
        assertEquals(sqlregex.getSql(), "SELECT * FROM table");
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
        assertEquals(exception.getMessage(), "SQL-Input-String should have more characters than null.");
    }

    @Test
    void setEmptyRegex(){
        SqlRegex sqlregex = new SqlRegex();
        Exception exception = assertThrows(NullPointerException.class, () -> sqlregex.setRegex(""));
        assertEquals(exception.getMessage(), "REGEX-Output-String should have more characters than null.");
    }

    @Test
    void initSqlRegexGetRegex(){
        SqlRegex sqlregex = new SqlRegex();
        assertTrue(sqlregex.getRegex().isEmpty());
    }
}
