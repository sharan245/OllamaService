package com.ollamaService.rest.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by: Sharan MH
 * on: 25/08/25
 */

@Configuration
public class WebConfig {

    @Value("${server.allow-origin}")
    private String urls;
    @Bean
    public WebMvcConfigurer corsConfiguration() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull  CorsRegistry registry) {
                registry.addMapping("/**")   // allow all endpoints
                        .allowedOrigins(urls)
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("*");
            }
        };
    }

}
