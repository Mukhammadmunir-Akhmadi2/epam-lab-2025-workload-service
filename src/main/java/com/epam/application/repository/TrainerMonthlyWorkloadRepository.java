package com.epam.application.repository;

import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;

import java.util.List;
import java.util.Optional;

public interface TrainerMonthlyWorkloadRepository {
    List<TrainerMonthlyWorkload> findAllByTrainer(TrainerSummary trainer);
    Optional<TrainerMonthlyWorkload> findByTrainerAndYearAndMonth(TrainerSummary trainer, int year, int month);
    TrainerMonthlyWorkload save(TrainerMonthlyWorkload monthly);
}