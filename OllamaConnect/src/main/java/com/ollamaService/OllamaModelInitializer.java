package com.ollamaService;

import com.ollamaService.config.OllamaConfig;
import com.ollamaService.config.OllamaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 21/10/25
 */
@Component
public class OllamaModelInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(OllamaModelInitializer.class);
    private final Function<String, String> loadResourceFile;
    private final OllamaConfig ollamaConfig;
    @Autowired
    private OllamaProperties ollamaProperties;


    public OllamaModelInitializer(Function<String, String> loadResourceFile, OllamaConfig ollamaConfig) {
        this.loadResourceFile = loadResourceFile;
        this.ollamaConfig = ollamaConfig;
    }

    @Value("${ollama.auto-create-model:true}")
    private boolean autoCreate;

    @Override
    public void run(String... args){
        try {
            if (!autoCreate) {
                log.info("Ollama model auto-create disabled by configuration.");
                return;
            }

            // Create RestClient
            RestClient client = ollamaConfig.restClientBuilder().clone().build();

            // Build request
            Map<String, Object> body = Map.of(
                    "model", ollamaProperties.getModel(),
                    "from", ollamaProperties.getFrom(),
                    "template", loadResourceFile.apply("ollamaModelConfig/Template"),
                    "system", loadResourceFile.apply("ollamaModelConfig/System"),
                    /*"parameters",Map.of("num_thread", ollamaProperties.getNumThread()),*/
                    "stream", false
            );

            //no need to drop older model because model with same name:tag will override older
            // Send POST request
            String response = client.post()
                    .uri(ollamaProperties.getUrl() + "/api/create")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("Ollama model created: " + ollamaProperties.getModel());
            log.debug(response);
        } catch (Exception e) {
            log.error("Error While Creating Model: " + ollamaProperties.getModel(),e);
            throw e;
        }
    }
}