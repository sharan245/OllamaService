package com.ollamaService.model.repo.mcp;

import com.ollamaService.model.mongoCollections.mcp.McpMessageHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Repository
public interface McpMessageHistoryRepo extends MongoRepository<McpMessageHistory, ObjectId> {


    @Aggregation(pipeline = {
            "{ '$group': { '_id': '$conversationId' } }",
            "{ '$skip': ?0 }",
            "{ '$limit': ?1 }"
    })
    List<String> getAllConversationId(int skip, int limit);

}
