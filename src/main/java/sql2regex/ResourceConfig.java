package sql2regex;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    public class ResourcesConfig {
        private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/static/"};

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
