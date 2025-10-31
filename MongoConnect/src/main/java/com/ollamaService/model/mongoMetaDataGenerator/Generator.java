package com.ollamaService.model.mongoMetaDataGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollamaService.lazyOperation.MongoLazyOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by: Sharan MH
 * on: 22/10/25
 */
public class Generator {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(MongoLazyOperation.class);

    public static String toJsonSchema(Class<?> clazz) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generateSchema(clazz));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schema", e);
        }
    }

    public static Map<String, Object> generateSchema(Class<?> clazz) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        schema.put("properties", properties);

        for (Field field : clazz.getDeclaredFields()) {
            MongoField annotation = field.getAnnotation(MongoField.class);
            if (annotation == null) {
                log.warn(field.getName() + " is not annotated with MongoField will not be consider for generating Schema");
                continue;
            }

            Class<?> fieldType = field.getType();
            Map<String, Object> fieldSchema = new LinkedHashMap<>();

            // --- ENUM HANDLING ---
            if (fieldType.isEnum()) {
                fieldSchema.put("type", "string");
                if (!annotation.description().isEmpty())
                    fieldSchema.put("description", annotation.description());
                fieldSchema.put("enum", getEnumValues(fieldType));
            }

            // --- BUILTIN TYPES ---
            else if (isBuiltinType(fieldType)) {
                fieldSchema.put("type", mapToJsonType(fieldType));
                if (!annotation.description().isEmpty())
                    fieldSchema.put("description", annotation.description());
            }

            // --- COLLECTION (List / Set etc.) ---
            else if (Collection.class.isAssignableFrom(fieldType)) {
                fieldSchema.put("type", "array");
                if (!annotation.description().isEmpty())
                    fieldSchema.put("description", annotation.description());

                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType paramType) {
                    Type elementType = paramType.getActualTypeArguments()[0];
                    if (elementType instanceof Class<?> elementClass) {
                        fieldSchema.put("items", describeType(elementClass));
                    }
                } else {
                    fieldSchema.put("items", Map.of("type", "object"));
                }
            }

            // --- MAP ---
            else if (Map.class.isAssignableFrom(fieldType)) {
                fieldSchema.put("type", "object");
                if (!annotation.description().isEmpty())
                    fieldSchema.put("description", annotation.description());

                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType paramType) {
                    Type valueType = paramType.getActualTypeArguments()[1];
                    if (valueType instanceof Class<?> valueClass) {
                        fieldSchema.put("additionalProperties", describeType(valueClass));
                    }
                } else {
                    fieldSchema.put("additionalProperties", Map.of("type", "object"));
                }
            }
            // --- COMPLEX OBJECTS (recursive) ---
            else {
                fieldSchema.put("type", "object");
                if (!annotation.description().isEmpty())
                    fieldSchema.put("description", annotation.description());
                fieldSchema.put("properties", generateSchema(fieldType).get("properties"));
            }

            properties.put(field.getName(), fieldSchema);
        }

        return schema;
    }

    private static Map<String, Object> describeType(Class<?> type) {
        if (type.isEnum()) {
            return Map.of(
                    "type", "string",
                    "enum", getEnumValues(type)
            );
        } else if (isBuiltinType(type)) {
            return Map.of("type", mapToJsonType(type));
        } else {
            return Map.of(
                    "type", "object",
                    "properties", generateSchema(type).get("properties")
            );
        }
    }

    private static List<String> getEnumValues(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        List<String> values = new ArrayList<>();
        for (Object c : constants) values.add(c.toString());
        return values;
    }

    private static boolean isBuiltinType(Class<?> type) {
        return type.isPrimitive()
                || Number.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time");
    }

    private static String mapToJsonType(Class<?> type) {
        if (String.class.isAssignableFrom(type)) return "string";
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) return "number";
        if (Boolean.class.isAssignableFrom(type) || type == boolean.class) return "boolean";
        if (Date.class.isAssignableFrom(type) || type.getName().startsWith("java.time")) return "string";
        return "object";
    }
}
