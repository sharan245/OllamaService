package com.ollamaService.model.repo.mcp;

import com.ollamaService.model.mongoCollections.mcp.McpMessageSummary;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Repository
public interface McpMessageSummaryRepo extends MongoRepository<McpMessageSummary, ObjectId> {

    @Query(value = "{ 'conversationId': ?0 }", fields = "{ 'conversationId': 1, '_id': 1 }")
    McpMessageSummary findProjectedByConversationId(String conversationId);

}
