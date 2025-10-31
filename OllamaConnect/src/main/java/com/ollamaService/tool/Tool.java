package com.ollamaService.tool;


import java.lang.annotation.*;

/**
 * created by sharan
 * Interfaces to maintain all tool
 */
public interface Tool extends AnnotatedTool {
    String getName();

    String getDescription();

    @Override
    default boolean isBuiltInTooling(){
        return false;
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Call {
    }
}
