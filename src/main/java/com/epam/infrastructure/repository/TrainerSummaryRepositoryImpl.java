package com.epam.infrastructure.repository;

import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
import com.epam.infrastructure.persistence.TrainerSummaryDocumentRepository;
import com.epam.model.TrainerTrainingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainerSummaryRepositoryImpl implements TrainerSummaryRepository {

    private final TrainerSummaryDocumentRepository docRepo;
    private final TrainerTrainingSummaryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerTrainingSummary> findByUsername(String username) {
        return docRepo.findByUsername(username).map(mapper::toModel);
    }

    @Override
    @Transactional
    public TrainerTrainingSummary save(TrainerTrainingSummary trainer) {
        var saved = docRepo.save(mapper.toDao(trainer));
        return mapper.toModel(saved);
    }
}