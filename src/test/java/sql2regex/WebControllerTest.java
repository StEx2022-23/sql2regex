package sql2regex;

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
class WebControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void testExampleSite() throws Exception {
        mvc.perform(get("/examples").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testAboutSite() throws Exception {
        mvc.perform(get("/about").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testStartSite() throws Exception {
        mvc.perform(get("/").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testImpressumSite() throws Exception {
        mvc.perform(get("/impressum").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testPrivacySite() throws Exception {
        mvc.perform(get("/privacy").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testRobotsSite() throws Exception {
        mvc.perform(get("/robots.txt").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testSitemapSite() throws Exception {
        mvc.perform(get("/sitemap.xml").contentType(MediaType.TEXT_XML)).andExpect(status().isOk());
    }

}
