package com.epam.infrastructure.persistance;

import com.epam.infrastructure.daos.TrainerMonthlyWorkloadDao;
import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.infrastructure.persistence.TrainerComponent;
import com.epam.infrastructure.persistence.TrainerMonthlyWorkloadComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TrainerMonthlyWorkloadComponentTest {

    @Autowired
    private TrainerComponent trainerComponent;

    @Autowired
    private TrainerMonthlyWorkloadComponent monthlyComponent;

    @Test
    void findAllByTrainer_returnsAllMonthlyRows_forTrainer() {
        TrainerSummaryDao trainer = new TrainerSummaryDao();
        trainer.setUsername("trainer.one");
        trainer.setFirstName("Trainer");
        trainer.setLastName("One");
        trainer.setStatus(TrainerStatus.ACTIVE);
        trainer.setActive(true);

        trainer = trainerComponent.saveAndFlush(trainer);

        TrainerMonthlyWorkloadDao jan = new TrainerMonthlyWorkloadDao();
        jan.setTrainer(trainer);
        jan.setYear(2026);
        jan.setMonth(1);
        jan.setTotalDuration(120);

        TrainerMonthlyWorkloadDao feb = new TrainerMonthlyWorkloadDao();
        feb.setTrainer(trainer);
        feb.setYear(2026);
        feb.setMonth(2);
        feb.setTotalDuration(90);

        monthlyComponent.saveAndFlush(jan);
        monthlyComponent.saveAndFlush(feb);

        List<TrainerMonthlyWorkloadDao> rows = monthlyComponent.findAllByTrainer(trainer);

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(TrainerMonthlyWorkloadDao::getMonth)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void findByTrainerAndYearAndMonth_returnsRow_whenExists() {
        TrainerSummaryDao trainer = new TrainerSummaryDao();
        trainer.setUsername("trainer.two");
        trainer.setFirstName("Trainer");
        trainer.setLastName("Two");
        trainer.setStatus(TrainerStatus.ACTIVE);
        trainer.setActive(true);

        trainer = trainerComponent.saveAndFlush(trainer);

        TrainerMonthlyWorkloadDao row = new TrainerMonthlyWorkloadDao();
        row.setTrainer(trainer);
        row.setYear(2026);
        row.setMonth(2);
        row.setTotalDuration(300);

        monthlyComponent.saveAndFlush(row);

        Optional<TrainerMonthlyWorkloadDao> found =
                monthlyComponent.findByTrainerAndYearAndMonth(trainer, 2026, 2);

        assertThat(found).isPresent();
        assertThat(found.get().getTotalDuration()).isEqualTo(300);
    }
}
