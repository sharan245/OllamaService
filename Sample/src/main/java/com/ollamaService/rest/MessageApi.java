package com.ollamaService.rest;

import com.ollamaService.helper.FluxRecorder;
import com.ollamaService.helper.HelperMethods;
import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import com.ollamaService.model.mongoCollections.mcp.McpMessageHistory;
import com.ollamaService.model.nonCollectionsModels.mcp.MessageEntity;
import com.ollamaService.service.MessageServices;
import com.ollamaService.service.SummarizeHistoryServices;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by: Sharan MH
 * on: 25/08/25
 */

@RestController
@RequestMapping("/message")
public class MessageApi {

    private final MessageServices messageServices;
    private final SummarizeHistoryServices messageHistoryServices;
    private static final Logger log = LoggerFactory.getLogger(MessageApi.class);

    @Autowired
    private HelperMethods helperMethods;

    public MessageApi(MessageServices messageServices, SummarizeHistoryServices messageHistoryServices) {
        this.messageServices = messageServices;
        this.messageHistoryServices = messageHistoryServices;
    }

    @PostMapping("/getExistingConversion")
    public ResponseEntity<List<String>> getExistingConversion() {
        log.debug("getExistingConversion id request");
        List<String> conversationIds = messageHistoryServices.getAllConversationId();
        log.debug("available conversion ids:\n {}", conversationIds);
        return ResponseEntity.ok(conversationIds);
    }

    @PostMapping("/createNewConversion")
    public ResponseEntity<NewConversionResponse> createNewConversion() {
        log.debug("new Conversion Id creation request");
        String conversationId = messageHistoryServices.registerNewConversionHistory();
        log.debug("new Conversation Id registered: " + conversationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new NewConversionResponse(conversationId));
    }

    public record NewConversionResponse(
            String conversationId
    ) {
    }

    @PostMapping("/openOlderConversion")
    public ResponseEntity<List<McpMessageHistory>> openOlderConversion(
            @Valid @RequestBody OlderConversionRequest request
    ) {
        List<McpMessageHistory> mcpMessageHistories;
        try {
            log.debug("openOlderConversion request {}", request);
            messageHistoryServices.registerConversionHistoryIntoMemory(request.conversationId);
            mcpMessageHistories = messageHistoryServices.getMessageHistory(request.conversationId);
        } catch (Exception e) {
            log.error("Severity: {} Unable to load older conversion Id: {}", McpApplicationErrorLogs.Severity.HIGH, request.conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(mcpMessageHistories);
    }

    public record OlderConversionRequest(
            @NotBlank String conversationId
    ) {
    }

    @PostMapping(value = "/continueConversation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<CustomChatResponse>>> continueConversation(
            @Valid @RequestBody ContinueConversationRequest request
    ) {
        log.debug("User asked question: {}", request);
        final AtomicReference<String> conversationId = new AtomicReference<>(request.conversationId);

        if (conversationId.get() == null) {
            conversationId.set(createNewConversion().getBody().conversationId);
        } else if (!messageHistoryServices.isConversionHistoryRegistered(conversationId.get())) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        MessageEntity question = MessageEntity.builder().messageType(MessageType.USER).content(request.message).build();
        FluxRecorder<ChatResponse> recorder = new FluxRecorder<>();

        return Mono.just(ResponseEntity.ok(recorder
                .record(messageServices.continueStreamingConversionWithTools(conversationId.get(), request.message))
                .doOnComplete(() -> {
                    McpMessageHistory mcpMessageHistory = McpMessageHistory.builder()
                            .conversationId(conversationId.get())
                            .question(question)
                            .answer(MessageEntity.builder().messageType(MessageType.ASSISTANT).content(recorder.getRecordedTillNowAsString(v -> v.getResult().getOutput().getText())).build()).build();

                    messageHistoryServices.storeMessageHistory(mcpMessageHistory);
                })
                .doFinally(s -> {
                    if (!s.equals(SignalType.ON_COMPLETE)) {
                        log.debug("continueConversation api not ended properly: {}", s);
                    }
                }).map(CustomChatResponse::new)
        ));
    }

    public record ContinueConversationRequest(
            String conversationId,
            @NotBlank String message
    ) {
    }

    public record CustomChatResponse(
            MessageType messageType,
            String text
    ) {
        public CustomChatResponse(MessageType messageType, String text) {
            this.messageType = messageType;
            this.text = text;
        }

        public CustomChatResponse(ChatResponse chatResponse) {
            this(chatResponse.getResult().getOutput().getMessageType(), chatResponse.getResult().getOutput().getText());
            log.debug("Response Meta Data" +chatResponse.getMetadata());
        }

        @Override
        public String toString() {
            return messageType + ": " + text + "\n";
        }
    }
}
