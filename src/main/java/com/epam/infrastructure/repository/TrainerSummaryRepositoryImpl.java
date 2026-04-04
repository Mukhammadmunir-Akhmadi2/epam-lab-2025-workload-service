package com.epam.infrastructure.repository;

import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
import com.epam.infrastructure.persistence.TrainerSummaryDocumentRepository;
import com.epam.model.TrainerTrainingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Profile({"local", "stg", "test", "prod"})
public class TrainerSummaryRepositoryImpl implements TrainerSummaryRepository {

    private final TrainerSummaryDocumentRepository docRepo;
    private final TrainerTrainingSummaryMapper mapper;

    @Override
    public Optional<TrainerTrainingSummary> findByUsername(String username) {
        return docRepo.findByUsername(username).map(mapper::toModel);
    }

    @Override
    public TrainerTrainingSummary save(TrainerTrainingSummary trainer) {
        var saved = docRepo.save(mapper.toDao(trainer));
        return mapper.toModel(saved);
    }
}