package sqltoregex;

import org.junit.jupiter.api.Test;
import sqltoregex.converter.MultiSqlRegex;
import sqltoregex.converter.SqlRegex;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MultiSqlRegexTest {

    @Test
    void checkIfConvertingProduceSomeOutput() {
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
        for (SqlRegex sqlregex : multisqlregex.getMultiSqlRegex()) {
            assertFalse(sqlregex.getRegex().isEmpty());
        }
    }

    @Test
    void setListNotEmpty() {
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
}
