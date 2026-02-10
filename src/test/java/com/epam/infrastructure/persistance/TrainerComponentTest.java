package com.epam.infrastructure.persistance;

import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.infrastructure.persistence.TrainerComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TrainerComponentTest {

    @Autowired
    private TrainerComponent trainerComponent;

    @Test
    void findByUsername_returnsTrainer_whenExists() {
        TrainerSummaryDao trainer = new TrainerSummaryDao();
        trainer.setUsername("john.smith");
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setStatus(TrainerStatus.ACTIVE);
        trainer.setActive(true);

        trainerComponent.saveAndFlush(trainer);

        Optional<TrainerSummaryDao> found = trainerComponent.findByUsername("john.smith");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.smith");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(trainerComponent.findByUsername("missing.user")).isEmpty();
    }
}