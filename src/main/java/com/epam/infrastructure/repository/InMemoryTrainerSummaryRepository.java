package com.epam.infrastructure.repository;

import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.model.TrainerTrainingSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class InMemoryTrainerSummaryRepository implements TrainerSummaryRepository {

    private final Map<String, TrainerTrainingSummary> store = new HashMap<>();
    private final ObjectMapper objectMapper;

    @Value("${app.dev.storage.path}")
    private String storagePath;

    @PostConstruct
    private void loadFromFile() {
        File file = new File(storagePath);
        if (!file.exists()) {
            log.info("No existing storage file found at '{}', starting fresh.", storagePath);
            return;
        }
        try {
            TrainerTrainingSummary[] entries = objectMapper.readValue(file, TrainerTrainingSummary[].class);
            Arrays.stream(entries).forEach(t -> store.put(t.getUsername(), t));
            log.info("Loaded {} trainer summaries from '{}'", store.size(), storagePath);
        } catch (IOException e) {
            log.error("Failed to load storage file '{}': {}", storagePath, e.getMessage());
        }
    }

    @Override
    public Optional<TrainerTrainingSummary> findByUsername(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public TrainerTrainingSummary save(TrainerTrainingSummary trainer) {
        if (trainer.getTrainerId() == null) {
            trainer.setTrainerId(UUID.randomUUID().toString());
        }
        store.put(trainer.getUsername(), trainer);
        return trainer;
    }

    @PreDestroy
    private void persistToFile() {
        File file = new File(storagePath);
        file.getParentFile().mkdirs();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, store.values());
            log.debug("Persisted {} trainer summaries to '{}'", store.size(), storagePath);
        } catch (IOException e) {
            log.error("Failed to persist to file '{}': {}", storagePath, e.getMessage());
        }
    }
}