package com.epam.infrastructure.controllers.impl;

import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.controllers.WorkloadController;
import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkloadControllerImpl implements WorkloadController {

    private final WorkloadQueryServiceImpl queryService;

    @Override
    public ResponseEntity<TrainerMonthlySummaryResponseDto> getTrainerWorkloadSummary(String username) {
        return ResponseEntity.ok(queryService.getSummary(username));
    }
}
