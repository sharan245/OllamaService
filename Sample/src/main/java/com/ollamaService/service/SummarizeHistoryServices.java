package com.ollamaService.service;


import com.ollamaService.OllamaService;
import com.ollamaService.helper.HelperMethods;
import com.ollamaService.lazyOperation.MongoLazyOperation;
import com.ollamaService.memory.SummarizingChatMemory;
import com.ollamaService.memory.UserChatMemory;
import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import com.ollamaService.model.mongoCollections.mcp.McpMessageHistory;
import com.ollamaService.model.mongoCollections.mcp.McpMessageSummary;
import com.ollamaService.model.nonCollectionsModels.mcp.MessageEntity;
import com.ollamaService.model.repo.mcp.McpMessageHistoryRepo;
import com.ollamaService.model.repo.mcp.McpMessageSummaryRepo;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by: Sharan MH
 * on: 21/08/25
 */

@Service
public class SummarizeHistoryServices {
    private static final Logger log = LoggerFactory.getLogger(SummarizeHistoryServices.class);
    private final OllamaService.NonStreamingService.WithOutTools nonStreamingWithOutTools;
    private final McpMessageSummaryRepo repo;
    private final McpMessageHistoryRepo historyRepo;
    private final MongoLazyOperation mongoLazyOperation;
    private final UserChatMemory userChatMemory;

    @Autowired
    private HelperMethods.MessageToMapConverter messageToMapConverter;

    @Autowired
    @Qualifier("generateConversationId")
    private Supplier<String> generateConversationId;

    @Value("${mcp.token.maxHistoryMessage}")
    private Integer maxHistoryMessage;
    @Value("${mcp.token.lastNMessagesToRemain}")
    private Integer lastNMessagesToRemain;

    public SummarizeHistoryServices(OllamaService.NonStreamingService.WithOutTools nonStreamingWithOutTools, McpMessageSummaryRepo repo, McpMessageHistoryRepo historyRepo, MongoLazyOperation mongoLazyOperation, UserChatMemory userChatMemory) {
        this.nonStreamingWithOutTools = nonStreamingWithOutTools;
        this.repo = repo;
        this.historyRepo = historyRepo;
        this.mongoLazyOperation = mongoLazyOperation;
        this.userChatMemory = userChatMemory;
    }

    public boolean isConversionHistoryRegistered(@NonNull String conversationId) {
        return userChatMemory.exists(conversationId);
    }

    @NonNull
    public List<String> getAllConversationId() {
        return historyRepo.getAllConversationId(0, 100);
    }

    @NonNull
    public String registerNewConversionHistory() {
        String conversationId = generateConversationId.get();
        registerConversionHistoryIntoMemory(conversationId);
        return conversationId;
    }

    /**
     * @param conversationId register conversion history first before asking question
     */
    public void registerConversionHistoryIntoMemory(@NonNull String conversationId) {
        userChatMemory.add(conversationId, () -> SummarizingChatMemory.builder(getCustomSummarizer())
                .chatMemoryRepository(getMemoryRepoWithHistory(conversationId))
                .chatMemoryAfterUpdate(onMessageUpdateInMemory())
                .maxMessages(maxHistoryMessage)
                .lastNMessagesToRemain(lastNMessagesToRemain).build()
        );
    }


    public void storeMessageHistory(@NonNull McpMessageHistory mcpMessageHistory) {
        mongoLazyOperation.pushAndForget(() -> historyRepo.insert(mcpMessageHistory));
    }

    public List<McpMessageHistory> getMessageHistory(@NonNull String conversationId) {
        return historyRepo.findAll(
                Example.of(McpMessageHistory.builder().conversationId(conversationId).build()),
                Sort.by(Sort.Direction.ASC, "_id")
        );
    }

    private ChatMemoryRepository getMemoryRepoWithHistory(@NonNull String conversationId) {
        return repo.findOne(
                Example.of(McpMessageSummary.builder().conversationId(conversationId).build())
        ).map(
                history -> {
                    InMemoryChatMemoryRepository repo = new InMemoryChatMemoryRepository();
                    repo.saveAll(conversationId, MessageEntity.toMessageList(history.getMessages()));
                    return repo;
                }
        ).orElseGet(InMemoryChatMemoryRepository::new);
    }

    @NonNull
    private BiConsumer<String, List<Message>> onMessageUpdateInMemory() {
        return (conversationId, messageList) ->
                mongoLazyOperation.pushAndForget(() -> {
                    McpMessageSummary summaryRepo = repo.findProjectedByConversationId(conversationId);
                    if (summaryRepo != null) {
                        summaryRepo.setMessages(MessageEntity.toMessageEntity(messageList));
                        repo.save(summaryRepo);
                    } else {
                        repo.insert(
                                McpMessageSummary.builder()
                                        .conversationId(conversationId)
                                        .messages(MessageEntity.toMessageEntity(messageList)).build()
                        );
                    }
                });
    }

    @NonNull
    private Function<List<Message>, String> getCustomSummarizer() {
        return messageList -> {
            try {
                return nonStreamingWithOutTools.question("Summarize all the conversations to save token.", messageList);
            } catch (Exception e) {
                log.error("severity:{} Unable to get summary for older conversation", McpApplicationErrorLogs.Severity.HIGH, messageToMapConverter.convert(messageList), e);
            }
            return "Summary cannot be provided";
        };
    }


}
