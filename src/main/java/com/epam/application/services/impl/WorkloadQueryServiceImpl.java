package com.epam.application.services.impl;

import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.application.services.WorkloadQueryService;
import com.epam.model.TrainerTrainingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@Service
@Log4j2
@RequiredArgsConstructor
public class WorkloadQueryServiceImpl implements WorkloadQueryService {

    private final TrainerSummaryRepository trainerRepository;

    @Override
    public TrainerTrainingSummary getSummary(String username) {
        TrainerTrainingSummary trainerSummary = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        log.info("Workload summary returned. trainer={}, years={}", username, trainerSummary.getYears().size());

        return trainerSummary;
    }
}
