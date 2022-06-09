package sqltoregex;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Resource configurer.
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    /**
     * Handle resources.
     */
    public static class ResourcesConfig {
        private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/static/"};

        /**
         * Allow access to sitemap.xml and robots.txt.
         * @param registry autowired ResourceHandlerRegistry, no action required here
         */
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/sitemap.xml")
                    .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                    .setCachePeriod(3000);
            registry.addResourceHandler("/robots.txt")
                    .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                    .setCachePeriod(3000);
        }
    }
}
