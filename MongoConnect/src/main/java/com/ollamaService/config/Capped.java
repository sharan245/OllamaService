package com.ollamaService.config;



import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import com.ollamaService.model.mongoCollections.mcp.McpToolCallHistory;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@AllArgsConstructor
enum Capped {

    /**
     * collections will drop and re-create if it's already exists
     */
    MCP_TOOL_LOGS(McpToolCallHistory.class, 1_073_741_824L), //1GB
    MCP_APPLICATION_LOGS(McpApplicationErrorLogs.class, 104_857_600L); //100MB

    final Class<?> coll;
    final long maxSize;

    // Create capped collection
    private void createCapCollection(MongoTemplate mongoTemplate) {
        mongoTemplate.createCollection(coll, CollectionOptions.empty().capped().size(maxSize));
    }

    // convert to capped collection
    private void covertToCapCollection(MongoTemplate mongoTemplate) {
        Document command = new Document();
        command.put("convertToCapped", coll.getSimpleName()); // collection name
        command.put("size", maxSize); // size in bytes
        Document result = mongoTemplate.executeCommand(command);

        if (result.getDouble("ok") != 1.0) {
            throw new IllegalStateException("Couldn't Convert " + coll.getSimpleName() + " collection to capped " + result);
        }
    }

    static void createCappedCollection(MongoTemplate mongoTemplate) {
        Arrays.stream(Capped.values()).forEach(c -> {
            if (!mongoTemplate.collectionExists(c.coll)) {
                c.createCapCollection(mongoTemplate);
            } else {
                // Verify if it's capped already
                Document stats = mongoTemplate.executeCommand("{ collStats: \"" + c.coll.getSimpleName() + "\" }");
                if (!stats.getBoolean("capped", false)) {
                    c.covertToCapCollection(mongoTemplate);
                }
            }
        });
    }
}

