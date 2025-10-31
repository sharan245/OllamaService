package com.ollamaService.model.mongoCollections.mcp;

import com.ollamaService.model.mongoCollections.MongoCollection;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@Document("McpToolCallHistory")
public class McpToolCallHistory extends MongoCollection {

    @Indexed
    private String conversationId;
    private String userId;

    @Indexed(partialFilter = "{ 'status': 'ERROR' }")
    private Status status;

    private String question;
    private String response;

    private Instant questionAskedTime;
    private Instant responseTime;
    @Indexed
    private Long timeTakenToCompleteInSec;

    private List<ToolExecutionTime> toolExecutionTime;
    private List<Map<String, Object>> toolCalls;


    public enum Status {
        SUCCESS,
        ERROR
    }

    public record ToolExecutionTime(
        String toolName,
        Long executionTimeInSec
    ){

    }
}
