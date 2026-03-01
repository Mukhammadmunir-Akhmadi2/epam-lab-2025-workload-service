package com.epam.infrastructure.persistence;

import com.epam.infrastructure.daos.TrainerTrainingSummaryDao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerSummaryDocumentRepository extends MongoRepository<TrainerTrainingSummaryDao, UUID> {
    Optional<TrainerTrainingSummaryDao> findByUsername(String username);

}
