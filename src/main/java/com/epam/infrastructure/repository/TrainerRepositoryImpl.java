package com.epam.infrastructure.repository;

import com.epam.application.repository.TrainerRepository;
import com.epam.infrastructure.mappers.TrainerMapper;
import com.epam.infrastructure.persistence.TrainerComponent;
import com.epam.model.TrainerSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepository {

    private final TrainerComponent jpaRepo;
    private final TrainerMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerSummary> findByUsername(String username) {
        return jpaRepo.findByUsername(username).map(mapper::toModel);
    }

    @Override
    @Transactional
    public TrainerSummary save(TrainerSummary trainer) {
        var saved = jpaRepo.save(mapper.toDao(trainer));
        return mapper.toModel(saved);
    }
}