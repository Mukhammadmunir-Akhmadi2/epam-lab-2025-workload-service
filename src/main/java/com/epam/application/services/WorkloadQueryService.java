package com.epam.application.services;

import com.epam.model.TrainerTrainingSummary;

public interface WorkloadQueryService {
    TrainerTrainingSummary getSummary(String username);
}
