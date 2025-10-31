package com.ollamaService.model.mongoCollections.mcp;

import com.ollamaService.model.mongoCollections.MongoCollection;
import com.ollamaService.model.nonCollectionsModels.mcp.MessageEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Created by: Sharan MH
 * on: 25/08/25
 */

@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@Document("McpMessageSummary")
public class McpMessageSummary extends MongoCollection {

    @Indexed(unique = true)
    private String conversationId;

    @LastModifiedDate
    private Instant summarizedTime;

    private List<MessageEntity> messages;

}
