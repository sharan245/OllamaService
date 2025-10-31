package com.ollamaService.tool;

/**
 * Created by: Sharan MH
 * on: 13/08/25
 */

public interface AnnotatedTool {
    boolean useToolForLLM();

    default boolean isBuiltInTooling(){
        return true;
    }
}
