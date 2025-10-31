package com.ollamaService.model.nonCollectionsModels;

import com.ollamaService.model.mongoMetaDataGenerator.MongoField;
import lombok.Data;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */
@Data
public class Aggregator {
    @MongoField(description = "cell/equipmentComponent for which this KPI is recorded")
    String equipmentComponent;
    @MongoField(description = "type of KPI that is recorded")
    String cause;
}
