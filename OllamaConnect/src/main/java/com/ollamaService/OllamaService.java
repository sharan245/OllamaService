package com.ollamaService;


import com.ollamaService.memory.UserChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;


/**
 * created by sharan
 */


@Service
public class OllamaService {
    private final OllamaChatModel ollamaChatModel;
    private final OllamaChatOptions ollamaOptionsWithTools;
    private final OllamaChatOptions ollamaOptionsWithOutTools;
    private final ChatClient chatClient;

    @Autowired
    public OllamaService(
            OllamaChatModel ollamaChatModel,
            OllamaChatOptions ollamaOptions,
            ChatClient chatClient
    ) {
        this.ollamaChatModel = ollamaChatModel;
        this.ollamaOptionsWithTools = ollamaOptions;
        this.chatClient = chatClient;

        OllamaChatOptions temp = ollamaOptions.copy();
        temp.setToolContext(new HashMap<>());
        temp.setToolCallbacks(new ArrayList<>());
        temp.setToolNames(new HashSet<>());

        this.ollamaOptionsWithOutTools = temp;
    }

    @Service
    public class StreamingService {

        @Service
        public class WithOutTools {
            public Flux<ChatResponse> question(String question) {
                return question(question, Collections.emptyList());
            }

            public Flux<ChatResponse> question(String question, List<Message> oldMessages) {
                List<Message> updates = new ArrayList<>(oldMessages);
                updates.add(new UserMessage(question));
                return ollamaChatModel.stream(new Prompt(updates, ollamaOptionsWithOutTools));
            }
        }

        @Service
        public class WithTools {
            public Flux<ChatResponse> question(String question) {
                return question(question, Collections.emptyList(), new HashMap<>());
            }

            public Flux<ChatResponse> question(String question, Map<String, Object> toolContext) {
                return question(question, Collections.emptyList(), toolContext);
            }

            public Flux<ChatResponse> question(String question, List<Message> oldMessages, Map<String, Object> toolContext) {
                ollamaOptionsWithTools.setToolContext(toolContext);

                List<Message> updates = new ArrayList<>(oldMessages);
                updates.add(new UserMessage(question));

                return ollamaChatModel.stream(new Prompt(updates, ollamaOptionsWithOutTools));
            }
        }

        @Service
        public class MemoryDependentService {
            @Autowired
            UserChatMemory userChatMemory;

            public Flux<ChatResponse> question(String conversationId, String question, Map<String, Object> toolContext, List<Message> oldMessages) {
                return chatClient.prompt()
                        .toolContext(toolContext)
                        .advisors(userChatMemory.get(conversationId))
                        .messages(oldMessages)
                        .user(question)
                        .stream()
                        .chatResponse();
            }

            public Flux<ChatResponse> question(String conversationId, String question, Map<String, Object> toolContext) {
                return chatClient.prompt()
                        .toolContext(toolContext)
                        .advisors(userChatMemory.get(conversationId))
                        .user(question)
                        .stream()
                        .chatResponse();
            }
        }
    }

    @Service
    public class NonStreamingService {
        @Service
        public class WithOutTools {
            public String question(String question) {
                return question(question, Collections.emptyList());
            }

            public String question(String question, List<Message> oldMessages) {
                List<Message> updates = new ArrayList<>(oldMessages);
                updates.add(new UserMessage(question));
                return ollamaChatModel.call(new Prompt(updates, ollamaOptionsWithOutTools)).getResult().getOutput().getText();
            }
        }

        @Service
        public class WithTools {
            public String question(String question) {
                return question(question, Collections.emptyList(), new HashMap<>());
            }

            public String question(String question, Map<String, Object> toolContext) {
                return question(question, Collections.emptyList(), toolContext);
            }

            public String question(String question, List<Message> oldMessages, Map<String, Object> toolContext) {
                ollamaOptionsWithTools.setToolContext(toolContext);

                List<Message> updates = new ArrayList<>(oldMessages);
                updates.add(new UserMessage(question));

                return ollamaChatModel.call(new Prompt(updates, ollamaOptionsWithOutTools)).getResult().getOutput().getText();
            }
        }

        @Service
        public class MemoryDependentService {
            @Autowired
            UserChatMemory userChatMemory;

            public String question(String conversationId, String question, Map<String, Object> toolContext, List<Message> oldMessages) {
                return chatClient.prompt()
                        .toolContext(toolContext)
                        .advisors(userChatMemory.get(conversationId))
                        .messages(oldMessages)
                        .user(question)
                        .call()
                        .chatResponse().getResult().getOutput().getText();
            }

            public String question(String conversationId, String question, Map<String, Object> toolContext) {
                return chatClient.prompt()
                        .toolContext(toolContext)
                        .advisors(userChatMemory.get(conversationId))
                        .user(question)
                        .call()
                        .chatResponse().getResult().getOutput().getText();
            }
        }
    }
}

