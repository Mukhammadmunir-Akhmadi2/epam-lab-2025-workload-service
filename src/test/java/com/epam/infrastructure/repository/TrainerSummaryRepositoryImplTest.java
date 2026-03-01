package com.epam.infrastructure.repository;

import com.epam.infrastructure.daos.TrainerTrainingSummaryDao;
import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
import com.epam.infrastructure.persistence.TrainerSummaryDocumentRepository;
import com.epam.model.TrainerTrainingSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@Import({
        TrainerSummaryRepositoryImpl.class,
        TrainerSummaryRepositoryImplTest.MapperTestConfig.class
})
class TrainerSummaryRepositoryImplTest {

    @Autowired
    TrainerSummaryRepositoryImpl trainerRepository;

    @Autowired
    TrainerSummaryDocumentRepository trainerJpaRepository;

    @TestConfiguration
    static class MapperTestConfig {
        @Bean
        TrainerTrainingSummaryMapper trainerMapper() {
            return Mappers.getMapper(TrainerTrainingSummaryMapper.class);
        }
    }


    @BeforeEach
    void cleanDb() {
        trainerJpaRepository.deleteAll();
    }

    @Test
    void findByUsername_returnsTrainer_whenExists() {
        TrainerTrainingSummaryDao dao = new TrainerTrainingSummaryDao();
        dao.setUsername("trainer.one");
        dao.setFirstName("Trainer");
        dao.setLastName("One");
        dao.setStatus(true);
        dao.setActive(true);
        dao = trainerJpaRepository.save(dao);

        Optional<TrainerTrainingSummary> found = trainerRepository.findByUsername("trainer.one");

        assertTrue(found.isPresent());
        assertEquals(dao.getTrainerId().toString(), found.get().getTrainerId());
        assertEquals("trainer.one", found.get().getUsername());
        assertEquals("Trainer", found.get().getFirstName());
        assertEquals("One", found.get().getLastName());
        assertEquals(true, found.get().getStatus());
        assertTrue(found.get().getActive());
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        Optional<TrainerTrainingSummary> found = trainerRepository.findByUsername("missing.user");
        assertTrue(found.isEmpty());
    }

    @Test
    void save_persistsTrainer_andReturnsMappedModel() {
        TrainerTrainingSummary input = new TrainerTrainingSummary();
        input.setUsername("trainer.saved");
        input.setFirstName("Saved");
        input.setLastName("Trainer");
        input.setStatus(true);
        input.setActive(true);

        TrainerTrainingSummary saved = trainerRepository.save(input);

        assertNotNull(saved);
        assertNotNull(saved.getTrainerId());
        assertEquals("trainer.saved", saved.getUsername());
        assertEquals("Saved", saved.getFirstName());
        assertEquals("Trainer", saved.getLastName());
        assertEquals(true, saved.getStatus());
        assertTrue(saved.getActive());

        List<TrainerTrainingSummaryDao> all = trainerJpaRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("trainer.saved", all.get(0).getUsername());
        assertEquals("Saved", all.get(0).getFirstName());
        assertEquals("Trainer", all.get(0).getLastName());
        assertEquals(true, all.get(0).getStatus());
        assertTrue(all.get(0).getActive());
    }
}