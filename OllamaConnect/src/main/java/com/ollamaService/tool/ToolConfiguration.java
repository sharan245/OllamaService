package com.ollamaService.tool;


import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */

@Configuration
public class ToolConfiguration {


    /**
     * bean for annotated Tools
     */
    @Primary
    @Bean
    public List<MethodToolCallbackProvider> annotatedTools(List<AnnotatedTool> annotatedTools) {
        return annotatedTools.stream()
                .filter(AnnotatedTool::useToolForLLM)
                .filter(AnnotatedTool::isBuiltInTooling)
                .map(tool -> MethodToolCallbackProvider.builder().toolObjects(tool).build())
                .toList();
    }


    /**
     * bean for custom Tool with more control
     */
    @Bean
    public List<ToolCallback> toolCallbacks(List<Tool> tools) {
        return tools.stream()
                .filter(Tool::useToolForLLM)
                .map(tool -> (ToolCallback) getMethodToolCallback(tool))
                .collect(Collectors.toList());
    }


    private MethodToolCallback getMethodToolCallback(Tool tool) {
        Method method = Arrays.stream(ReflectionUtils.getDeclaredMethods(tool.getClass()))
                .filter(m -> m.isAnnotationPresent(Tool.Call.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No method with annotation " + Tool.Call.class.getSimpleName() + " found in " + tool.getName()));

        return MethodToolCallback.builder()
                .toolDefinition(
                        ToolDefinition
                                .builder()
                                .name(tool.getName())
                                .description(tool.getDescription())
                                .inputSchema(JsonSchemaGenerator.generateForMethodInput(method))
                                .build()
                )
                .toolMetadata(ToolMetadata.from(method))
                .toolMethod(method)
                .toolObject(tool)
                .toolCallResultConverter(new DefaultToolCallResultConverter())
                .build();
    }
}
