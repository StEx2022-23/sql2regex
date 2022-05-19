package sqltoregex.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyErrorControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void test4xxError() throws Exception {
        mvc.perform(get("/errortestpage").contentType(MediaType.TEXT_HTML)).andExpect(status().is4xxClientError());
    }
}
