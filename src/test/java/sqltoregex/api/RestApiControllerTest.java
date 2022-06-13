package sqltoregex.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestApiControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void testConvertRequest() throws Exception {
        ApiConvertObject apiConvertObject = new ApiConvertObject();
        apiConvertObject.setSettingsType(SettingsType.ALL);
        List<String> sqlList = new LinkedList<>();
        sqlList.add("SELECT *");
        sqlList.add("SELECT *");
        apiConvertObject.setSql(sqlList);

        MvcResult result = this.mvc.perform(MockMvcRequestBuilders
                        .post("/api/convert", 42L)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(apiConvertObject)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json"))
                        .andReturn();

        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.getResponse().getContentAsString().length());

        String response = result.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);

        Assertions.assertEquals("[\"SELECT *\",\"SELECT *\"]", json.getString("sql"));
        Assertions.assertEquals("ALL", json.getString("settingsType"));
        Assertions.assertNotEquals(0, json.getString("regex").length());
    }

    @Test
    void testSettingsTypesRequest() throws Exception {
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders
                        .post("/api/settingstypes", 42L)
                        .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json"))
                        .andReturn();

        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.getResponse().getContentAsString().length());

        String response = result.getResponse().getContentAsString();
        for(SettingsType settingsType : SettingsType.values()){
            if(settingsType.equals(SettingsType.USER)) Assertions.assertFalse(response.contains(settingsType.toString()));
            else Assertions.assertTrue(response.contains(settingsType.toString()));
        }
    }

    @Test
    void testSettingsOptionsRequest() throws Exception {
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders
                        .post("/api/settingsoptions", 42L)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.getResponse().getContentAsString().length());

        String response = result.getResponse().getContentAsString();
        for(SettingsOption settingsOption : SettingsOption.values()){
            Assertions.assertTrue(response.contains(settingsOption.toString()));
        }
    }

    @Test
    void testSpecificSettingsOptionsRequest() throws Exception {
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders
                        .post("/api/specificsettingsoption", 42L)
                        .contentType("application/json")
                        .content("ALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.getResponse().getContentAsString().length());

        String response = result.getResponse().getContentAsString();
        for(SettingsOption settingsOption : new SettingsManager().getSettingsContainer(SettingsType.ALL).getAllSettings().keySet()){
            Assertions.assertTrue(response.contains(settingsOption.toString()));
        }
    }

    @Test
    void testDocsRequest() throws Exception {
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders
                        .post("/api/docs", 42L)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andReturn();

        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.getResponse().getContentAsString().length());
    }
}
