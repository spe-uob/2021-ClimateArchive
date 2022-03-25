package org.climatearchive.climatearchive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${allowed_cors}")
    private String myAllowedApi;

    @Value("${allowed_cors_separator}")
    private String sep;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/getData")
                .allowedMethods("GET").allowedOrigins(myAllowedApi.split(sep));
    }
}