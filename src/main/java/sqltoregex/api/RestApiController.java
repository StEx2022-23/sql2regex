package sqltoregex.api;

import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.SettingsType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Rest-Api-Controller for Converting.
 */
@RestController
@RequestMapping("/api")
public class RestApiController {
    public static final String LISTSTYLE = "   - ";
    public static final String LINEBREAK = "\n";

    @PostMapping("/docs")
    public String getAllEndpoints(){
        StringBuilder endpoints = new StringBuilder();
        endpoints.append("Available endpoints:").append(LINEBREAK);
        endpoints.append(LISTSTYLE).append("/convert").append(LINEBREAK);
        endpoints.append(LISTSTYLE).append("/settingstypes").append(LINEBREAK);
        endpoints.append(LISTSTYLE).append("/settingsoptions").append(LINEBREAK);
        endpoints.append(LISTSTYLE).append("/specificsettingsoption").append(LINEBREAK);
        return endpoints.toString();
    }

    /**
     * Performs rest api requests on "/api".
     * @return json with converting results
     */
    @PostMapping("/convert")
    public ApiConvertObject convertSingleRequestWithDefaultSchoolPreset(@RequestBody ApiConvertObject apiConvertObject){
        return apiConvertObject;
    }

    /**
     * Print all available setting types.
     * @return list of all {@link SettingsType}
     */
    @PostMapping("/settingstypes")
    public List<SettingsType> returnSettingsTypes(){
        List<SettingsType> settingsType = new LinkedList<>();
        for(SettingsType setType : SettingsType.values()) {
            if(!setType.equals(SettingsType.USER)) settingsType.add(setType);
        }
        return settingsType;
    }

    /**
     * Print all available settings options.
     * @return list of all {@link SettingsOption}
     */
    @PostMapping("/settingsoptions")
    public SettingsOption[] returnSettingsOptions(){
        return SettingsOption.values();
    }

    /**
     * Print specific set of {@link SettingsOption} filtered by {@link SettingsType}.
     * @param settingsType {@link SettingsType} as string in request body
     * @return list of specific {@link SettingsOption}
     * @throws XPathExpressionException if xml parsing error occurs
     * @throws ParserConfigurationException if xml parsing error occurs
     * @throws IOException if xml parsing error occurs
     * @throws URISyntaxException if xml parsing error occurs
     * @throws SAXException if xml parsing error occurs
     */
    @PostMapping("/specificsettingsoption")
    public Set<SettingsOption> returnSpecificSettingsOptions(@RequestBody String settingsType) throws XPathExpressionException, ParserConfigurationException, IOException, URISyntaxException, SAXException {
        SettingsManager settingsManager = new SettingsManager();
        return settingsManager.getSettingsContainer(SettingsType.valueOf(settingsType)).getAllSettings().keySet();
    }
}