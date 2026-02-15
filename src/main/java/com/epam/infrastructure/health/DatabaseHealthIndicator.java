package com.epam.infrastructure.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Health health() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
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
