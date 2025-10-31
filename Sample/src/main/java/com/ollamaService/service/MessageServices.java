package com.ollamaService.service;

import com.ollamaService.OllamaService;
import com.ollamaService.helper.FluxRecorder;
import com.ollamaService.helper.HelperMethods;
import com.ollamaService.lazyOperation.MongoLazyOperation;
import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import com.ollamaService.model.mongoCollections.mcp.McpToolCallHistory;
import com.ollamaService.model.repo.mcp.McpToolCallHistoryRepository;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by: Sharan MH
 * on: 21/08/25
 */

@Service
public class MessageServices {
    private static final Logger log = LoggerFactory.getLogger(MessageServices.class);
    private final OllamaService.StreamingService.MemoryDependentService memoryDependentService;
    private final McpToolCallHistoryRepository repo;
    private final MongoLazyOperation mongoLazyOperation;
    @Autowired
    private HelperMethods.MessageToMapConverter messageToMapConverter;
    private static final String DEFAULT_USER = "DEFAULT_USER";

    public MessageServices(OllamaService.StreamingService.MemoryDependentService memoryDependentService, McpToolCallHistoryRepository repo, MongoLazyOperation mongoLazyOperation) {
        this.memoryDependentService = memoryDependentService;
        this.repo = repo;
        this.mongoLazyOperation = mongoLazyOperation;
    }

    /**
     * conversion always maintains history, don't use this if it's a single question
     */
    @NonNull
    public Flux<ChatResponse> continueStreamingConversionWithTools(
            @NonNull String conversationId,
            @NonNull String question
    ) {
        Flux<ChatResponse> flux;
        McpToolCallHistory mcpToolCallHistory = McpToolCallHistory.builder()
                .userId(DEFAULT_USER)
                .question(question)
                .conversationId(conversationId)
                .toolExecutionTime(new ArrayList<>())
                .toolCalls(new ArrayList<>())
                .questionAskedTime(Instant.now()).build();
        try {
            log.debug("Question from user: " + DEFAULT_USER + ", conversationId: " + conversationId);
            flux = memoryDependentService.question(conversationId, question, Map.of(McpToolCallHistory.class.getSimpleName(), mcpToolCallHistory));
        } catch (Exception e) {
            tollCallHistoryFinalResponse(mcpToolCallHistory, McpToolCallHistory.Status.ERROR, e.getMessage());
            log.error("severity:{} Error Processing Question from user: " + DEFAULT_USER + ", conversationId: " + conversationId, McpApplicationErrorLogs.Severity.HIGH, messageToMapConverter.convert(mcpToolCallHistory), e);
            return Flux.error(e);
        }

        return handleLogs(flux, mcpToolCallHistory);
    }


    private Flux<ChatResponse> handleLogs(Flux<ChatResponse> flux, McpToolCallHistory mcpToolCallHistory) {
        FluxRecorder<ChatResponse> recorder = new FluxRecorder<>();

        return recorder.record(flux)
                .doOnError(e -> {
                    tollCallHistoryFinalResponse(mcpToolCallHistory, McpToolCallHistory.Status.ERROR, e.getMessage());
                    log.error("severity:{} Error Processing Question from user: " + DEFAULT_USER + ", conversationId: " + mcpToolCallHistory.getConversationId(), McpApplicationErrorLogs.Severity.HIGH, messageToMapConverter.convert(mcpToolCallHistory), e);
                }).doOnComplete(() -> {
                    String finalAnswer = recorder.getRecordedTillNowAsString(v -> v.getResult().getOutput().getText());
                    log.debug("Streaming complete final Answer: {}", finalAnswer);
                    tollCallHistoryFinalResponse(mcpToolCallHistory, McpToolCallHistory.Status.SUCCESS, finalAnswer);
                }).doFinally(s -> {
                    log.debug("Streaming ended reason: {}", s);
                    if (mcpToolCallHistory.getResponse() == null || mcpToolCallHistory.getResponse().isBlank()) {
                        tollCallHistoryFinalResponse(mcpToolCallHistory, McpToolCallHistory.Status.SUCCESS, s.toString());
                    }
                    mongoLazyOperation.pushAndForget(() -> repo.insert(mcpToolCallHistory));
                });
    }

    private void tollCallHistoryFinalResponse(McpToolCallHistory mcpToolCallHistory, McpToolCallHistory.Status status, String response) {
        mcpToolCallHistory.setStatus(status);
        mcpToolCallHistory.setResponse(response);
        mcpToolCallHistory.setResponseTime(Instant.now());
        mcpToolCallHistory.setTimeTakenToCompleteInSec(
                Duration.between(mcpToolCallHistory.getQuestionAskedTime(), mcpToolCallHistory.getResponseTime()).getSeconds()
        );
    }

}
