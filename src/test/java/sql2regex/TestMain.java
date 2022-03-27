package sql2regex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TestMain {
    @Autowired
    private MyErrorController myErrorController;

    @Autowired
    private ResourceConfig resourceConfig;

    @Autowired
    private WebController webController;

    @Autowired
    private RestApiController restApiController;

    @Test
    void main(){
        assertDoesNotThrow(() -> Sql2regexApplication.main(new String[] {}));
    }

    @Test
    void contextLoads() {
        assertNotNull(myErrorController);
        assertNotNull(resourceConfig);
        assertNotNull(webController);
        assertNotNull(restApiController);
    }
}
