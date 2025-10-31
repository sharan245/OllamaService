package com.ollamaService.model.mongoCollections.mcp;

import com.ollamaService.model.mongoCollections.MongoCollection;
import com.ollamaService.model.nonCollectionsModels.mcp.MessageEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by: Sharan MH
 * on: 25/08/25
 */

@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@Document("McpMessageHistory")
public class McpMessageHistory extends MongoCollection {

    @Indexed
    private String conversationId;

    private MessageEntity question;
    private MessageEntity answer;
}
