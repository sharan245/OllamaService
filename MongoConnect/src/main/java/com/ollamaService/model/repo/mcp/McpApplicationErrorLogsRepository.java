package com.ollamaService.model.repo.mcp;


import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Repository
public interface McpApplicationErrorLogsRepository extends MongoRepository<McpApplicationErrorLogs, ObjectId> {


}
