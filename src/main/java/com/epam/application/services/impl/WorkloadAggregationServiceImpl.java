package com.epam.application.services.impl;

import com.epam.application.repository.TrainerMonthlyWorkloadRepository;
import com.epam.application.repository.TrainerRepository;
import com.epam.application.services.WorkloadAggregationService;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.enums.ActionType;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class WorkloadAggregationServiceImpl implements WorkloadAggregationService {

    private final TrainerRepository trainerRepository;
    private final TrainerMonthlyWorkloadRepository monthlyRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM')")
    public void applyEvent(TrainerWorkloadRequestDto req) {
        TrainerSummary trainer = trainerRepository.findByUsername(req.getTrainerUsername())
                .orElseGet(() -> {
                    TrainerSummary t = new TrainerSummary();
                    t.setUsername(req.getTrainerUsername());
                    return t;
                });

        trainer.setFirstName(req.getTrainerFirstName());
        trainer.setLastName(req.getTrainerLastName());
        trainer.setActive(Boolean.TRUE.equals(req.getIsActive()));
        trainer = trainerRepository.save(trainer);

        int year = req.getTrainingDate().getYear();
        int month = req.getTrainingDate().getMonthValue();

        TrainerSummary finalTrainer = trainer;
        TrainerMonthlyWorkload monthly = monthlyRepository
                .findByTrainerAndYearAndMonth(trainer, year, month)
                .orElseGet(() -> {
                    TrainerMonthlyWorkload m = new TrainerMonthlyWorkload();
                    m.setTrainer(finalTrainer);
                    m.setYear(year);
                    m.setMonth(month);
                    m.setTotalDuration(0);
                    return m;
                });

        int delta = req.getActionType() == ActionType.ADD ? req.getTrainingDuration() : -req.getTrainingDuration();
        int newTotal = monthly.getTotalDuration() + delta;

        if (newTotal < 0) newTotal = 0;

        monthly.setTotalDuration(newTotal);
        monthlyRepository.save(monthly);

        log.info(
                "Workload event applied successfully: trainerUsername={}, year={}, month={}, totalDuration={}",
                req.getTrainerUsername(), year, month, newTotal
        );
    }
}
