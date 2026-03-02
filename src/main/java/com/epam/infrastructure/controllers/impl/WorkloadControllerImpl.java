package com.epam.infrastructure.controllers.impl;

import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.controllers.WorkloadController;
import com.epam.infrastructure.dtos.TrainerTrainingSummaryResponseDto;

import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
import com.epam.model.TrainerTrainingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkloadControllerImpl implements WorkloadController {

    private final WorkloadQueryServiceImpl queryService;
    private final TrainerTrainingSummaryMapper trainerSummaryMapper;

    @Override
    public ResponseEntity<TrainerTrainingSummaryResponseDto> getTrainerWorkloadSummary(String username) {
        TrainerTrainingSummary trainerSummary = queryService.getSummary(username);
        return ResponseEntity.ok(trainerSummaryMapper.toDto(trainerSummary));
    }
}
