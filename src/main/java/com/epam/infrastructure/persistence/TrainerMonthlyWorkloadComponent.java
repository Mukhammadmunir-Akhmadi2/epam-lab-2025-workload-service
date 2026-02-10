package com.epam.infrastructure.persistence;

import com.epam.infrastructure.daos.TrainerMonthlyWorkloadDao;
import com.epam.infrastructure.daos.TrainerSummaryDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerMonthlyWorkloadComponent extends JpaRepository<TrainerMonthlyWorkloadDao, UUID> {
    List<TrainerMonthlyWorkloadDao> findAllByTrainer(TrainerSummaryDao trainer);

    Optional<TrainerMonthlyWorkloadDao> findByTrainerAndYearAndMonth(TrainerSummaryDao trainer, int year, int month);

}
