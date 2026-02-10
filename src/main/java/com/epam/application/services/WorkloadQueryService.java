package com.epam.application.services;

import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;

public interface WorkloadQueryService {
    TrainerMonthlySummaryResponseDto getSummary(String username);
}
