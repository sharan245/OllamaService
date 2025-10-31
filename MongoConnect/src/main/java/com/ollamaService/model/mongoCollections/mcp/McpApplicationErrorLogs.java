package com.ollamaService.model.mongoCollections.mcp;

import com.ollamaService.model.mongoCollections.MongoCollection;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Created by: Sharan MH
 * on: 22/08/25
 */

@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@Document("McpApplicationErrorLogs")
public class McpApplicationErrorLogs extends MongoCollection {
    @Indexed
    private Severity severity;

    @Indexed
    @CreatedDate
    private Instant occurredTime;

    private String applicationMsg;
    private Object[] additionInfo;

    private String exceptionClassName;
    private String exceptionMessage;
    private String stackTrace;


    public enum Severity{
        HIGH,
        WARN,
        DEFAULT
    }
}
