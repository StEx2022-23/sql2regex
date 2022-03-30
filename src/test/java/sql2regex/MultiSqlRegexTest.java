package sql2regex;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import sql2regex.converter.MultiSqlRegex;
import sql2regex.converter.SqlRegex;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class MultiSqlRegexTest {
    @Autowired
    MockMvc mvc;

    @Test
    void setListNotEmpty(){
        SqlRegex testSqlRegexEins = new SqlRegex();
        SqlRegex testSqlRegexZwei = new SqlRegex();
        String testInput = "SELECT * FROM table";
        testSqlRegexEins.setSql(testInput);
        testSqlRegexZwei.setSql(testInput);
        List<SqlRegex> sqlregexlist = new LinkedList<>();
        sqlregexlist.add(testSqlRegexEins);
        sqlregexlist.add(testSqlRegexZwei);

        MultiSqlRegex multisqlregex = new MultiSqlRegex(sqlregexlist);
        assertFalse(multisqlregex.getMultiSqlRegex().isEmpty());
    }

    @Test
    void checkIfConvertingProduceSomeOutput(){
        SqlRegex testSqlRegexEins = new SqlRegex();
        SqlRegex testSqlRegexZwei = new SqlRegex();
        String testInput = "SELECT * FROM table";
        testSqlRegexEins.setSql(testInput);
        testSqlRegexZwei.setSql(testInput);
        List<SqlRegex> sqlregexlist = new LinkedList<>();
        sqlregexlist.add(testSqlRegexEins);
        sqlregexlist.add(testSqlRegexZwei);

        MultiSqlRegex multisqlregex = new MultiSqlRegex(sqlregexlist);
        multisqlregex.convert();
        for (SqlRegex sqlregex : multisqlregex.getMultiSqlRegex()){
            assertFalse(sqlregex.getRegex().isEmpty());
        }
    }
}
