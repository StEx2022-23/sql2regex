package sql2regex;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sql2regex.converter.MultiSqlRegex;
import sql2regex.converter.SqlRegex;
import java.util.LinkedList;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestApiControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void testConvertGetMappingWithEmptyArguments() throws Exception {
        this.mvc.perform(get("/convert")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Pass your SQL-Statement!")));
    }


    @Test
    void testConvertPostMappingForAjaxRequest() throws Exception {
        SqlRegex testSqlRegex= new SqlRegex();
        String testInput = "SELECT * FROM table";
        String RelatedJsonFormat = "{\"sql\":\"SELECT * \"}";
        testSqlRegex.setSql(testInput);
        testSqlRegex.convert();

        this.mvc.perform(MockMvcRequestBuilders.post("/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RelatedJsonFormat)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(testSqlRegex.getRegex())));
    }

    @Test
    void testConvertPostMappingForMultiRequests() throws Exception {
        SqlRegex testSqlRegexEins = new SqlRegex();
        SqlRegex testSqlRegexZwei = new SqlRegex();
        String testInput = "SELECT * FROM table";
        String RelatedJsonFormat = "[{\"sql\": \"SELECT * FROM table\"}, {\"sql\": \"SELECT * FROM table\"}]";
        testSqlRegexEins.setSql(testInput);
        testSqlRegexZwei.setSql(testInput);

        List<SqlRegex> sqlregexlist = new LinkedList<>();
        sqlregexlist.add(testSqlRegexEins);
        sqlregexlist.add(testSqlRegexZwei);
        MultiSqlRegex multisqlregex = new MultiSqlRegex(sqlregexlist);
        multisqlregex.convert();

        this.mvc.perform(MockMvcRequestBuilders.post("/multiconvert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RelatedJsonFormat)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(multisqlregex.toString())));
    }
}
