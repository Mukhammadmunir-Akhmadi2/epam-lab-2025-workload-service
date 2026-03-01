package com.epam.infrastructure.persistance;

import com.epam.infrastructure.daos.TrainerTrainingSummaryDao;
import com.epam.infrastructure.persistence.TrainerSummaryDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class TrainerSummaryDocumentRepositoryTest {

    @Autowired
    private TrainerSummaryDocumentRepository trainerRepository;

    @Test
    void findByUsername_returnsTrainer_whenExists() {
        // given
        TrainerTrainingSummaryDao trainer = new TrainerTrainingSummaryDao();
        trainer.setTrainerId(UUID.randomUUID().toString());
        trainer.setUsername("john.smith");
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setStatus(true);
        trainer.setActive(true);

        trainerRepository.save(trainer);

        // when
        Optional<TrainerTrainingSummaryDao> found = trainerRepository.findByUsername("john.smith");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.smith");
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getStatus()).isTrue();
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        // when
        Optional<TrainerTrainingSummaryDao> found = trainerRepository.findByUsername("missing.user");

        // then
        assertThat(found).isEmpty();
    }
}