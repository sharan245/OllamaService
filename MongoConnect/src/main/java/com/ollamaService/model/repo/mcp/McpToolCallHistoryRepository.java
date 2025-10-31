package com.ollamaService.model.repo.mcp;

import com.ollamaService.model.mongoCollections.mcp.McpToolCallHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Repository
public interface McpToolCallHistoryRepository extends MongoRepository<McpToolCallHistory, ObjectId> {

    //(only ERROR logs)
   /* @Tailable
    @Query("{ 'status': 'ERROR' }")
    Stream<McpToolCallHistory> findErrorLogs();*/

    @Query("{ 'conversationId': ?0 }")
    @Update("{ '$push': { 'toolCalls': ?1 } }")
    void addNewToolCall(String conversationId, String toolCall);

    @Query("{ 'conversationId': ?0 }")
    @Update("{ '$set': { 'status': ?1, 'response': ?2 } }")
    void updateResponseForConversion(String conversationId, McpToolCallHistory.Status status, String response);
}
