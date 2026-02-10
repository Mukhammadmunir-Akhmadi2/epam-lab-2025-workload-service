package com.epam.application.services;

import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;

public interface WorkloadAggregationService {
    void applyEvent(TrainerWorkloadRequestDto event);
}