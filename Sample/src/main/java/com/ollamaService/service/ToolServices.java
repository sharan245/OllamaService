package com.ollamaService.service;


import com.ollamaService.helper.HelperMethods;
import com.ollamaService.model.mongoCollections.mcp.McpToolCallHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

@Service
public class ToolServices {
    private final HelperMethods.MessageToMapConverter messageToMapConverter;
    private static final Logger log = LoggerFactory.getLogger(ToolServices.class);

    public ToolServices(HelperMethods.MessageToMapConverter messageToMapConverter) {
        this.messageToMapConverter = messageToMapConverter;
    }

    @Bean("insertToolToMcpLogs")
    public BiConsumer<McpToolCallHistory, List<Message>> insertToolToMcpLogs() {
        return (mcpToolCallHistory, toolMsg) -> mcpToolCallHistory.setToolCalls(messageToMapConverter.convert(toolMsg));
    }

    @Bean("insertToolContextToMcpLogs")
    public Consumer<ToolContext> insertToolContextToMcpLogs() {
        return toolContext -> insertToolToMcpLogs().accept((McpToolCallHistory) toolContext.getContext().get(McpToolCallHistory.class.getSimpleName()), toolContext.getToolCallHistory());
    }

    @Bean("getToolExecutionTime")
    public Function<ToolContext, List<McpToolCallHistory.ToolExecutionTime>> getToolExecutionTime() {
        return toolContext -> ((McpToolCallHistory) toolContext.getContext().get(McpToolCallHistory.class.getSimpleName())).getToolExecutionTime();
    }


}
