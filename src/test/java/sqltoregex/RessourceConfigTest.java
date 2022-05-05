package sqltoregex;

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
class RessourceConfigTest {
    @Autowired
    MockMvc mvc;

    @Test
    void testRobotsTxt() throws Exception {
        mvc.perform(get("/robots.txt").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testSitemap() throws Exception {
        mvc.perform(get("/sitemap.xml").contentType(MediaType.TEXT_XML)).andExpect(status().isOk());
    }
}


