package sqltoregex;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TestMain {
    @Autowired
    private MyErrorController myErrorController;

    @Autowired
    private ResourceConfig resourceConfig;

    @Autowired
    private SqlToRegexController sql2RegexController;

//    @Autowired
//    private RestApiController restApiController;

    @Test
    void contextLoads() {
        assertNotNull(myErrorController);
        assertNotNull(resourceConfig);
        assertNotNull(sql2RegexController);
//        assertNotNull(restApiController);
    }

    @Test
    void main() {
        assertDoesNotThrow(() -> SqlToRegexApplication.main(new String[]{}));
    }
}
