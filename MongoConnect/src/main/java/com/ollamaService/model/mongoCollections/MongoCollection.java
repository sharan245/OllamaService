package com.ollamaService.model.mongoCollections;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

/**
 * Created by: Sharan MH
 * on: 19/08/25
 */

@Data
public abstract class MongoCollection {
    @Id
    private ObjectId _id;
}
