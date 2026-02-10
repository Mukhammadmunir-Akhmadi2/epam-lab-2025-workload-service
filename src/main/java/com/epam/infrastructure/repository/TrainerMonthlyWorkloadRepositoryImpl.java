package com.epam.infrastructure.repository;

import com.epam.application.repository.TrainerMonthlyWorkloadRepository;
import com.epam.infrastructure.mappers.TrainerMapper;
import com.epam.infrastructure.mappers.TrainerMonthlyWorkloadMapper;
import com.epam.infrastructure.persistence.TrainerMonthlyWorkloadComponent;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainerMonthlyWorkloadRepositoryImpl implements TrainerMonthlyWorkloadRepository {

    private final TrainerMonthlyWorkloadComponent jpaRepo;
    private final TrainerMonthlyWorkloadMapper monthlyMapper;
    private final TrainerMapper trainerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TrainerMonthlyWorkload> findAllByTrainer(TrainerSummary trainer) {
        var trainerDao = trainerMapper.toDao(trainer);
        return jpaRepo.findAllByTrainer(trainerDao).stream()
                .map(monthlyMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerMonthlyWorkload> findByTrainerAndYearAndMonth(TrainerSummary trainer, int year, int month) {
        var trainerDao = trainerMapper.toDao(trainer);
        return jpaRepo.findByTrainerAndYearAndMonth(trainerDao, year, month)
                .map(monthlyMapper::toModel);
    }

    @Override
    @Transactional
    public TrainerMonthlyWorkload save(TrainerMonthlyWorkload monthly) {
        var saved = jpaRepo.save(monthlyMapper.toDao(monthly));
        return monthlyMapper.toModel(saved);
    }
}