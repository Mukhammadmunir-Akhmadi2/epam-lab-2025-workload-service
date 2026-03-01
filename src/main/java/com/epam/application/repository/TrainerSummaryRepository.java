package com.epam.application.repository;

import com.epam.model.TrainerTrainingSummary;

import java.util.Optional;

public interface TrainerSummaryRepository {
    Optional<TrainerTrainingSummary> findByUsername(String username);
    TrainerTrainingSummary save(TrainerTrainingSummary trainer);
}