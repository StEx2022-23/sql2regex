package sqltoregex.controller;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sqltoregex.settings.SettingsOption;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SqlToRegexControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void testAboutSite() throws Exception {
        mvc.perform(get("/about").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testConvertingAsset() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .post("/convert")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                                    new BasicNameValuePair("spellings", SettingsOption.KEYWORDSPELLING.toString()),
                                    new BasicNameValuePair("orders", SettingsOption.TABLENAMEORDER.toString()),
                                    new BasicNameValuePair("dateFormats", "yyyy-MM-dd"),
                                    new BasicNameValuePair("timeFormats", "HH:MM:SS"),
                                    new BasicNameValuePair("dateTimeFormats", "YYYY-MM-DD HH:MM:SS"),
                                    new BasicNameValuePair("sql", "SELECT *"),
                                    new BasicNameValuePair("aggregateFunctionLang", "Mittelwert; AVG")
                            ))))
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testExampleSite() throws Exception {
        mvc.perform(get("/examples").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
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

    @Test
    void testStartSite() throws Exception {
        mvc.perform(get("/").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    void testVisualizationSite() throws Exception {
        mvc.perform(get("/visualization").contentType(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }
}
