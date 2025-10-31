package com.ollamaService.resource;

import java.util.Base64;

/**
 * Created by: Sharan MH
 * on: 19/08/25
 */

public record Resource(
        String name,
        String description,
        String mime_type,
        String uri
) {


    public Resource(String name, String description, String mime_type, String uri) {
        this.name = name;
        this.description = description;
        this.mime_type = mime_type;
        this.uri = uri;
    }

    public Resource(String name, String description, String resource) {
        this(
                name,
                description,
                "application/json",
                "data:application/json;base64," + Base64.getEncoder().encodeToString(resource.getBytes())
        );
    }
}