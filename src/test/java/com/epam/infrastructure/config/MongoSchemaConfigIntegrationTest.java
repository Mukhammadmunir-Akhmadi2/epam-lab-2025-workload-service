package com.epam.infrastructure.config;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MongoSchemaConfigIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoSchemaConfig mongoSchemaConfig;

    @Test
    void createUserCollection_createsSchema_whenNotExists() {
        // ensure collection does not exist
        if (mongoTemplate.collectionExists("trainer_training_summary")) {
            mongoTemplate.dropCollection("trainer_training_summary");
        }

        // run config
        mongoSchemaConfig.createUserCollection();

        // check collection exists
        assertThat(mongoTemplate.collectionExists("trainer_training_summary")).isTrue();

        // inspect collection validator
        MongoDatabase db = mongoTemplate.getDb();
        Document collections = db.runCommand(new Document("listCollections", 1)
                .append("filter", new Document("name", "trainer_training_summary")));
        List<Document> firstBatch = (List<Document>) ((Document) collections.get("cursor")).get("firstBatch");
        assertThat(firstBatch).hasSize(1);

        Document collInfo = firstBatch.get(0);
        Document options = (Document) collInfo.get("options");
        assertThat(options).isNotNull();
        Document validator = (Document) options.get("validator");
        assertThat(validator).isNotNull();

        // check required top-level fields
        Document jsonSchema = (Document) validator.get("$jsonSchema");
        List<String> requiredFields = (List<String>) jsonSchema.get("required");
        assertThat(requiredFields).containsExactlyInAnyOrder("username", "status", "active", "years");

        // check properties exist
        Document properties = (Document) jsonSchema.get("properties");
        assertThat(properties.keySet()).contains("username", "firstName", "lastName", "status", "active", "years");

        // check years array schema
        Document years = (Document) properties.get("years");
        Document yearsItems = (Document) years.get("items");
        assertThat((List<String>) yearsItems.get("required")).containsExactlyInAnyOrder("year", "months");

        // check months array schema inside years
        Document months = (Document) ((Document) yearsItems.get("properties")).get("months");
        Document monthsItems = (Document) months.get("items");
        List<String> monthsRequired = (List<String>) monthsItems.get("required");
        assertThat(monthsRequired).containsExactlyInAnyOrder("month", "trainingsSummaryDuration");

        // check month range and trainingsSummaryDuration minimum
        Document monthProps = (Document) monthsItems.get("properties");
        Document monthField = (Document) monthProps.get("month");
        Document durationField = (Document) monthProps.get("trainingsSummaryDuration");

        assertThat(monthField.get("minimum")).isEqualTo(1);
        assertThat(monthField.get("maximum")).isEqualTo(12);
        assertThat(durationField.get("minimum")).isEqualTo(0);

        // check validation level and action (these are stored in collection options)
        assertThat(options.get("validationLevel")).isEqualTo("strict");
        assertThat(options.get("validationAction")).isEqualTo("error");
    }

    @Test
    void createUserCollection_skipsIfCollectionExists() {
        // create collection first
        if (!mongoTemplate.collectionExists("trainer_training_summary")) {
            mongoTemplate.createCollection("trainer_training_summary");
        }

        // call config (should not throw or recreate)
        mongoSchemaConfig.createUserCollection();

        // collection still exists
        assertThat(mongoTemplate.collectionExists("trainer_training_summary")).isTrue();
    }
}