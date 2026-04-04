package com.epam.infrastructure.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"local", "stg", "test", "prod"})
public class DatabaseHealthIndicator implements HealthIndicator {
    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return Health.up()
                    .withDetail("database", "reachable")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "unreachable")
                    .withException(e)
                    .build();
        }
    }
}
