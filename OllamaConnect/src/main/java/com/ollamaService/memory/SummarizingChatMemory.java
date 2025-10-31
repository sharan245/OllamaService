package com.ollamaService.memory;

import lombok.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 19/08/25
 */

public class SummarizingChatMemory implements ChatMemory {


    private final ChatMemoryRepository chatMemoryRepository;
    private final int maxMessages;
    private final int lastNMessagesToRemain;
    private final Function<List<Message>, String> summarizer;
    private final BiConsumer<String, List<Message>> chatMemoryAfterUpdate;

    public SummarizingChatMemory(ChatMemoryRepository chatMemoryRepository,
                                 int maxMessages,
                                 int lastNMessagesToRemain,
                                 Function<List<Message>, String> summarizer,
                                 BiConsumer<String, List<Message>> chatMemoryAfterUpdate) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
        Assert.notNull(summarizer, "summarizer cannot be null");

        this.chatMemoryRepository = chatMemoryRepository;
        this.maxMessages = maxMessages;
        this.lastNMessagesToRemain = lastNMessagesToRemain;
        this.summarizer = summarizer;
        this.chatMemoryAfterUpdate = chatMemoryAfterUpdate;
    }

    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> newMessages) {
        Assert.noNullElements(newMessages, "messages cannot contain null elements");

        List<Message> memoryMessages = chatMemoryRepository.findByConversationId(conversationId);

        List<Message> updated = new ArrayList<>(memoryMessages);
        updated.addAll(newMessages);

        if (updated.size() > maxMessages) {
            // Summarize everything older than lastNMessagesToSummarize
            int splitIndex = updated.size() - lastNMessagesToRemain;
            List<Message> toSummarize = updated.subList(0, splitIndex);
            String summaryText = summarizer.apply(toSummarize);

            // Keep only last N messages, plus the summary
            List<Message> trimmed = new ArrayList<>();
            trimmed.add(new SystemMessage("Summary of earlier conversation: " + summaryText));
            trimmed.addAll(
                    updated.subList(splitIndex, updated.size())
            );

            chatMemoryRepository.saveAll(conversationId, trimmed);
        } else {
            chatMemoryRepository.saveAll(conversationId, updated);
        }
        if (chatMemoryAfterUpdate != null)
            chatMemoryAfterUpdate.accept(conversationId, get(conversationId));
    }

    @Override
    @NonNull
    public List<Message> get(@NonNull String conversationId) {
        return chatMemoryRepository.findByConversationId(conversationId);
    }

    @Override
    public void clear(@NonNull String conversationId) {
        chatMemoryRepository.deleteByConversationId(conversationId);
    }


    // Builder for convenience (same style as MessageWindowChatMemory)
    public static Builder builder(Function<List<Message>, String> summarizer) {
        return new Builder(summarizer);
    }

    public static final class Builder {
        private ChatMemoryRepository repository = new InMemoryChatMemoryRepository();
        private int maxMessages = 10;
        private int lastNMessagesToRemain = 5;
        private BiConsumer<String, List<Message>> chatMemoryAfterUpdate = null;
        private final Function<List<Message>, String> summarizer;

        public Builder(Function<List<Message>, String> summarizer) {
            this.summarizer = summarizer;
        }

        public Builder chatMemoryRepository(ChatMemoryRepository repo) {
            this.repository = repo;
            return this;
        }

        public Builder chatMemoryAfterUpdate(BiConsumer<String, List<Message>> chatMemoryAfterUpdate) {
            this.chatMemoryAfterUpdate = chatMemoryAfterUpdate;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder lastNMessagesToRemain(int lastNMessagesToRemain) {
            this.lastNMessagesToRemain = lastNMessagesToRemain;
            return this;
        }

        public SummarizingChatMemory build() {
            return new SummarizingChatMemory(repository, maxMessages, lastNMessagesToRemain, summarizer, chatMemoryAfterUpdate);
        }
    }
}
