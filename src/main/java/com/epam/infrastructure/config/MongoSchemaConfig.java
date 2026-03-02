package com.epam.infrastructure.config;

import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;

@Configuration
@RequiredArgsConstructor
public class MongoSchemaConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createUserCollection() {

        if (!mongoTemplate.collectionExists("trainer_training_summary")) {

            MongoJsonSchema schema = MongoJsonSchema.builder()
                    .required("username", "status", "active", "years")
                    .properties(
                            JsonSchemaProperty.string("username"),
                            JsonSchemaProperty.string("firstName"),
                            JsonSchemaProperty.string("lastName"),
                            JsonSchemaProperty.bool("status"),
                            JsonSchemaProperty.bool("active"),
                            JsonSchemaProperty.array("years")
                                    .items(
                                            JsonSchemaObject.object()
                                                    .required("year", "months")
                                                    .properties(
                                                            JsonSchemaProperty.int32("year"),
                                                            JsonSchemaProperty.array("months")
                                                                    .items(
                                                                            JsonSchemaObject.object()
                                                                                    .required("month", "trainingsSummaryDuration")
                                                                                    .properties(
                                                                                            JsonSchemaProperty.int32("month").within(Range.closed(1, 12)),
                                                                                            JsonSchemaProperty.int64("trainingsSummaryDuration").gte(0)
                                                                                    )
                                                                    )
                                                    )
                                    )
                    )
                    .build();

            mongoTemplate.createCollection(
                    "trainer_training_summary",
                    CollectionOptions.empty()
                            .schema(schema)
                            .schemaValidationLevel(ValidationLevel.STRICT)
                            .schemaValidationAction(ValidationAction.ERROR)
            );
        }
    }
}