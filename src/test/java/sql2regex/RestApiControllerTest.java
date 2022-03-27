package sql2regex;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sql2regex.converter.SqlRegex;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.RequestEntity.post;
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
        testSqlRegex.setSql(testInput);
        testSqlRegex.convert();

        this.mvc.perform(MockMvcRequestBuilders.post("/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testInput)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(testSqlRegex.getRegex())));
    }

}
