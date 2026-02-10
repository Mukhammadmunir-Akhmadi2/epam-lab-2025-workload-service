package com.epam.application.repository;

import com.epam.model.TrainerSummary;

import java.util.Optional;

public interface TrainerRepository {
    Optional<TrainerSummary> findByUsername(String username);
    TrainerSummary save(TrainerSummary trainer);
}