package com.epam.infrastructure.repository;

import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.infrastructure.mappers.TrainerMapper;
import com.epam.infrastructure.persistence.TrainerJpaRepository;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        TrainerRepositoryImpl.class,
        TrainerRepositoryImplTest.MapperTestConfig.class
})
class TrainerRepositoryImplTest {

    @Autowired
    TrainerRepositoryImpl trainerRepository;

    @Autowired
    TrainerJpaRepository trainerJpaRepository;

    @TestConfiguration
    static class MapperTestConfig {
        @Bean
        TrainerMapper trainerMapper() {
            return Mappers.getMapper(TrainerMapper.class);
        }
    }

    @Test
    void findByUsername_returnsTrainer_whenExists() {
        // given: row exists in DB
        TrainerSummaryDao dao = new TrainerSummaryDao();
        dao.setUsername("trainer.one");
        dao.setFirstName("Trainer");
        dao.setLastName("One");
        dao.setStatus(TrainerStatus.ACTIVE);
        dao.setActive(true);
        dao = trainerJpaRepository.saveAndFlush(dao);

        // when
        Optional<TrainerSummary> found = trainerRepository.findByUsername("trainer.one");

        // then
        assertTrue(found.isPresent());
        assertEquals(dao.getTrainerId().toString(), found.get().getTrainerId());
        assertEquals("trainer.one", found.get().getUsername());
        assertEquals("Trainer", found.get().getFirstName());
        assertEquals("One", found.get().getLastName());
        assertEquals(TrainerStatus.ACTIVE, found.get().getStatus());
        assertTrue(found.get().getActive());
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        Optional<TrainerSummary> found = trainerRepository.findByUsername("missing.user");
        assertTrue(found.isEmpty());
    }

    @Test
    void save_persistsTrainer_andReturnsMappedModel() {
        // given
        TrainerSummary input = new TrainerSummary();
        input.setUsername("trainer.saved");
        input.setFirstName("Saved");
        input.setLastName("Trainer");
        input.setStatus(TrainerStatus.ACTIVE);
        input.setActive(true);

        // when
        TrainerSummary saved = trainerRepository.save(input);

        // then (returned model)
        assertNotNull(saved);
        assertNotNull(saved.getTrainerId()); // usually String UUID
        assertEquals("trainer.saved", saved.getUsername());
        assertEquals("Saved", saved.getFirstName());
        assertEquals("Trainer", saved.getLastName());
        assertEquals(TrainerStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.getActive());

        // and DB state
        List<TrainerSummaryDao> all = trainerJpaRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("trainer.saved", all.getFirst().getUsername());
        assertEquals("Saved", all.getFirst().getFirstName());
        assertEquals("Trainer", all.getFirst().getLastName());
        assertEquals(TrainerStatus.ACTIVE, all.getFirst().getStatus());
        assertTrue(all.getFirst().getActive());
    }
}