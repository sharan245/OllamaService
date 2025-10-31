package com.ollamaService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by: Sharan MH
 * on: 13/10/25
 */

@Component
@ConfigurationProperties(prefix = "ollama-service")
@Data
public class OllamaProperties {

    private String url;
    private String from;
    private String model;
    private String keepAlive;
    private Integer numThread;

}
