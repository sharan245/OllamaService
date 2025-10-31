package com.ollamaService.memory;

import com.ollamaService.OllamaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Component
public class UserChatMemory {

    protected static final Logger log = LoggerFactory.getLogger(UserChatMemory.class);
    private final Map<String, MessageChatMemoryAdvisor> conversationIdBasedMemory;
    private final OllamaService.NonStreamingService.WithOutTools nonStreamingWithOutTools;

    public UserChatMemory(OllamaService.NonStreamingService.WithOutTools nonStreamingWithOutTools) {
        this.nonStreamingWithOutTools = nonStreamingWithOutTools;
        this.conversationIdBasedMemory = Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, MessageChatMemoryAdvisor> eldest) {
                        return size() > 1000;
                    }
                }
        );
    }

    private Function<List<Message>, String> getDefaultSummarizer() {
        return messageList -> {
            try {
                return nonStreamingWithOutTools.question("Summarize all the conversations to save token.", messageList);
            } catch (Exception e) {
                log.error("Unable to get summary for older conversation for\n messageList:{}", messageList, e);
            }
            return "Summary cannot be provided";
        };
    }

    private MessageChatMemoryAdvisor getDefaultAdvisor(String conversationId) {
        return MessageChatMemoryAdvisor.builder(
                        SummarizingChatMemory.builder(getDefaultSummarizer()).build()
                ).conversationId(conversationId)
                .build();
    }

    public void add(String conversationId) {
        conversationIdBasedMemory.computeIfAbsent(conversationId, this::getDefaultAdvisor);
    }

    public void add(String conversationId, MessageChatMemoryAdvisor messageChatMemoryAdvisor) {
        conversationIdBasedMemory.computeIfAbsent(conversationId, cid -> messageChatMemoryAdvisor);
    }

    public void add(String conversationId, SummarizingChatMemory summarizingChatMemory) {
        conversationIdBasedMemory.computeIfAbsent(conversationId,
                cid -> MessageChatMemoryAdvisor.builder(summarizingChatMemory).conversationId(conversationId).build()
        );
    }

    public void add(String conversationId, Supplier<SummarizingChatMemory> summarizingChatMemory) {
        conversationIdBasedMemory.computeIfAbsent(conversationId,
                cid -> MessageChatMemoryAdvisor.builder(summarizingChatMemory.get()).conversationId(conversationId).build()
        );
    }

    public MessageChatMemoryAdvisor get(String conversationId) {
        return conversationIdBasedMemory.computeIfAbsent(conversationId, this::getDefaultAdvisor);
    }

    public boolean exists(String conversationId) {
        return conversationIdBasedMemory.containsKey(conversationId);
    }

    public void remove(String conversationId) {
        conversationIdBasedMemory.remove(conversationId);
    }
}
