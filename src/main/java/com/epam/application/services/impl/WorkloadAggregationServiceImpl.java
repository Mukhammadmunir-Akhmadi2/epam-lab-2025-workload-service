package com.epam.application.services.impl;

import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.application.services.WorkloadAggregationService;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.enums.ActionType;
import com.epam.model.TrainingMonthSummary;
import com.epam.model.TrainingYearSummary;
import com.epam.model.TrainerTrainingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class WorkloadAggregationServiceImpl implements WorkloadAggregationService {

    private final TrainerSummaryRepository trainerRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM')")
    public void applyEvent(TrainerWorkloadRequestDto req) {

        TrainerTrainingSummary trainer =
                trainerRepository.findByUsername(req.getTrainerUsername())
                        .orElseGet(() -> createNewTrainer(req));


        int year = req.getTrainingDate().getYear();
        int month = req.getTrainingDate().getMonthValue();

        TrainingYearSummary yearSummary = trainer.getYears()
                .stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    TrainingYearSummary newYear = new TrainingYearSummary();
                    newYear.setYear(year);
                    trainer.getYears().add(newYear);
                    return newYear;
                });

        TrainingMonthSummary monthSummary = yearSummary.getMonths()
                .stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    TrainingMonthSummary newMonth = new TrainingMonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setTrainingsSummaryDuration(0L);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        int delta = req.getActionType() == ActionType.ADD
                ? req.getTrainingDuration()
                : -req.getTrainingDuration();

        long newTotal = monthSummary.getTrainingsSummaryDuration() + delta;

        if (newTotal < 0) {
            newTotal = 0;
        }

        monthSummary.setTrainingsSummaryDuration(newTotal);

        trainerRepository.save(trainer);

        log.info(
                "Workload event applied successfully: trainerUsername={}, year={}, month={}, totalDuration={}",
                req.getTrainerUsername(), year, month, newTotal
        );
    }


    private TrainerTrainingSummary createNewTrainer(
            TrainerWorkloadRequestDto req) {

        log.info("Creating new trainer document username={}",
                 req.getTrainerUsername());
        TrainerTrainingSummary trainingSummary = new TrainerTrainingSummary();
        trainingSummary.setUsername(req.getTrainerUsername());
        trainingSummary.setFirstName(req.getTrainerFirstName());
        trainingSummary.setLastName(req.getTrainerLastName());
        trainingSummary.setActive(req.getIsActive());

        return trainingSummary;
    }
}
