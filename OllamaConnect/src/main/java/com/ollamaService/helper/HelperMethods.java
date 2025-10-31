package com.ollamaService.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by: Sharan MH
 * on: 22/08/25
 */

@Component
public class HelperMethods {

    @Bean("jsonConvertor")
    public ObjectMapper jsonConvertor() {
        return new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Bean("loadResourceFile")
    private static Function<String, String> loadResourceFile() {
        return path -> {
            try (var in = HelperMethods.class.getClassLoader().getResourceAsStream(path)) {
                return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Could not load resource file: " + path, e);
            }
        };
    }

    @Component
    public static class MessageToMapConverter {

        private final ObjectMapper objectMapper;

        public MessageToMapConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public List<Map<String, Object>> convert(List<Message> messages) {
            return messages.stream().map(this::convert).toList();
        }

        public Map<String, Object> convert(Message message) {
            return objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {
            });
        }

        public Map<String, Object> convert(Object object) {
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
            });
        }

        @Bean("generateConversationId")
        public Supplier<String> generateConversationId() {
            return () -> UUID.randomUUID().toString();
        }
    }
}
