package com.epam.infrastructure.controllers.impl;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.controllers.WorkloadInternalController;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkloadInternalControllerImpl implements WorkloadInternalController {
    private final WorkloadAggregationServiceImpl service;

    @Override
    public ResponseEntity<Void> acceptWorkloadEvent(TrainerWorkloadRequestDto request) {
        service.applyEvent(request);
        return ResponseEntity.ok().build();
    }
}
