package com.epam.infrastructure.controllers;

import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
@Hidden
public interface WorkloadInternalController {

    @PostMapping("/workload-events")
    ResponseEntity<Void> acceptWorkloadEvent(@RequestBody @Valid TrainerWorkloadRequestDto request);
}