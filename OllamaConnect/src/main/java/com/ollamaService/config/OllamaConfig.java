package com.ollamaService.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;


/**
 * created by sharan
 */

@Configuration
public class OllamaConfig {
    private static final Logger log = LoggerFactory.getLogger(OllamaConfig.class);
    @Autowired
    private OllamaProperties ollamaProperties;

    @Bean
    public OllamaChatOptions ollamaChatOptions(
            List<ToolCallback> toolCallbacks,
            List<MethodToolCallbackProvider> annotatedTools
    ) {
        log.debug("Tools that are Registered:");
        annotatedTools.stream().flatMap(v -> Arrays.stream(v.getToolCallbacks())).forEach(v -> log.debug(v.getToolDefinition().name()));
        toolCallbacks.forEach(v -> log.debug(v.getToolDefinition().name()));

        return OllamaChatOptions.builder()
                .model(ollamaProperties.getModel())
                .keepAlive(ollamaProperties.getKeepAlive())
                .numThread(ollamaProperties.getNumThread())
                .toolCallbacks(toolCallbacks)
                .toolCallbacks(annotatedTools.stream().flatMap(v -> Arrays.stream(v.getToolCallbacks())).toArray(ToolCallback[]::new))
                .build();
    }

    @Bean
    public OllamaApi ollamaApi() {
        return OllamaApi.builder()
                .baseUrl(ollamaProperties.getUrl())
                .responseErrorHandler(customResponseErrorHandler())
                .webClientBuilder(webClientBuilder())
                .restClientBuilder(restClientBuilder()).build();
    }

    @Bean
    public ChatClient customChatClient(
            OllamaChatModel ollamaChatModel,
            OllamaChatOptions ollamaChatOptions
    ) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(ollamaChatOptions)
                .build();
    }

    public ResponseErrorHandler customResponseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER.hasError(response);
            }

            @Override
            public void handleError(@NonNull URI url, @NonNull HttpMethod method, @NonNull ClientHttpResponse response) throws IOException {
                log.error("Ollama API error at " + method + " " + url + ": " + response.getStatusCode().value() + " - " + response.getStatusText());
                RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER.handleError(url, method, response);
            }

            @Override
            public void handleError(@NonNull ClientHttpResponse response) throws IOException {
                log.error("Ollama API error: " + response.getStatusCode().value() + " - " + response.getStatusText());
                RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER.handleError(response);
            }
        };
    }

    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {
                            log.debug("==== WEBCLIENT REQUEST SENT ====");
                            log.debug("Method: " + request.method());
                            log.debug("URL: " + request.url());
                            log.debug("Headers: " + request.headers());

                            return next.exchange(request)
                                    .doOnNext(response -> {
                                        log.debug("==== WEBCLIENT RESPONSE ====");
                                        log.debug("Status code: " + response.statusCode());
                                        log.debug("Headers: " + response.headers().asHttpHeaders());
                                    });
                        }
                );
    }


    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    log.debug("==== MCP REQUEST ====");
                    log.debug("Method: " + request.getMethod());
                    log.debug("URL: " + request.getURI());
                    log.debug("Headers: " + request.getHeaders());
                    log.debug("Body: " + new String(body));

                    ClientHttpResponse response = execution.execute(request, body);

                    log.debug("==== MCP RESPONSE ====");
                    log.debug("Status: " + response.getStatusCode());
                    log.debug("Headers: " + response.getHeaders());
                    //log.debug("Body: " + new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)); // careful, might need to buffer
                    return response;
                });
    }

    @Bean
    public RestClientCustomizer logRestCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestInterceptor((request, body, execution) -> {
                    log.debug("==== MCP REQUEST ====");
                    log.debug("Method: " + request.getMethod());
                    log.debug("URL: " + request.getURI());
                    log.debug("Headers: " + request.getHeaders());
                    log.debug("Body: " + new String(body));

                    ClientHttpResponse response = execution.execute(request, body);

                    log.debug("==== MCP RESPONSE ====");
                    log.debug("Status: " + response.getStatusCode());
                    log.debug("Headers: " + response.getHeaders());
                    //log.debug("Body: " + new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)); // careful, might need to buffer

                    return response;
                });
    }
}
