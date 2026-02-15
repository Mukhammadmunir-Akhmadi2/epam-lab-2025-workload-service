package com.epam.infrastructure.repository;

import com.epam.infrastructure.daos.TrainerMonthlyWorkloadDao;
import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.infrastructure.mappers.TrainerMapper;
import com.epam.infrastructure.mappers.TrainerMonthlyWorkloadMapper;
import com.epam.infrastructure.persistence.TrainerJpaRepository;
import com.epam.infrastructure.persistence.TrainerMonthlyWorkloadJpaRepository;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        TrainerMonthlyWorkloadRepositoryImpl.class,
        TrainerRepositoryImpl.class,
        TrainerMonthlyWorkloadRepositoryImplTest.MapperTestConfig.class

})
class TrainerMonthlyWorkloadRepositoryImplTest {

    @Autowired
    TrainerMonthlyWorkloadRepositoryImpl monthlyRepository;
    @Autowired TrainerRepositoryImpl trainerRepository;

    @Autowired
    TrainerMonthlyWorkloadJpaRepository monthlyComponent;
    @Autowired
    TrainerJpaRepository trainerJpaRepository;

    @TestConfiguration
    static class MapperTestConfig {
        @Bean
        TrainerMapper trainerMapper() {
            return org.mapstruct.factory.Mappers.getMapper(TrainerMapper.class);
        }

        @Bean
        TrainerMonthlyWorkloadMapper trainerMonthlyWorkloadMapper() {
            return org.mapstruct.factory.Mappers.getMapper(TrainerMonthlyWorkloadMapper.class);
        }
    }


    @Test
    void findAllByTrainer_returnsRows() {
        // create trainer in DB
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setUsername("trainer.one");
        trainerDao.setFirstName("Trainer");
        trainerDao.setLastName("One");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);
        trainerDao = trainerJpaRepository.saveAndFlush(trainerDao);

        // create monthly rows in DB
        TrainerMonthlyWorkloadDao jan = new TrainerMonthlyWorkloadDao();
        jan.setTrainer(trainerDao);
        jan.setYear(2026);
        jan.setMonth(1);
        jan.setTotalDuration(120);

        TrainerMonthlyWorkloadDao feb = new TrainerMonthlyWorkloadDao();
        feb.setTrainer(trainerDao);
        feb.setYear(2026);
        feb.setMonth(2);
        feb.setTotalDuration(90);

        monthlyComponent.saveAndFlush(jan);
        monthlyComponent.saveAndFlush(feb);

        // build domain trainer
        TrainerSummary trainerModel = new TrainerSummary();
        trainerModel.setTrainerId(trainerDao.getTrainerId().toString());
        trainerModel.setUsername(trainerDao.getUsername());
        trainerModel.setFirstName(trainerDao.getFirstName());
        trainerModel.setLastName(trainerDao.getLastName());
        trainerModel.setStatus(trainerDao.getStatus());
        trainerModel.setActive(trainerDao.getActive());

        // when
        List<TrainerMonthlyWorkload> result = monthlyRepository.findAllByTrainer(trainerModel);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getYear() == 2026 && r.getMonth() == 1 && r.getTotalDuration() == 120));
        assertTrue(result.stream().anyMatch(r -> r.getYear() == 2026 && r.getMonth() == 2 && r.getTotalDuration() == 90));
    }

    @Test
    void findAllByTrainer_returnsEmpty_whenNoRows() {
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setUsername("trainer.empty");
        trainerDao.setFirstName("Empty");
        trainerDao.setLastName("Trainer");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);
        trainerDao = trainerJpaRepository.saveAndFlush(trainerDao);

        TrainerSummary trainerModel = new TrainerSummary();
        trainerModel.setTrainerId(trainerDao.getTrainerId().toString());
        trainerModel.setUsername(trainerDao.getUsername());

        List<TrainerMonthlyWorkload> result = monthlyRepository.findAllByTrainer(trainerModel);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByTrainerAndYearAndMonth_returnsRow_whenExists() {
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setUsername("trainer.two");
        trainerDao.setFirstName("Trainer");
        trainerDao.setLastName("Two");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);
        trainerDao = trainerJpaRepository.saveAndFlush(trainerDao);

        TrainerMonthlyWorkloadDao row = new TrainerMonthlyWorkloadDao();
        row.setTrainer(trainerDao);
        row.setYear(2026);
        row.setMonth(3);
        row.setTotalDuration(300);
        monthlyComponent.saveAndFlush(row);

        TrainerSummary trainerModel = new TrainerSummary();
        trainerModel.setTrainerId(trainerDao.getTrainerId().toString());
        trainerModel.setUsername(trainerDao.getUsername());
        trainerModel.setFirstName(trainerDao.getFirstName());
        trainerModel.setLastName(trainerDao.getLastName());
        trainerModel.setStatus(trainerDao.getStatus());
        trainerModel.setActive(trainerDao.getActive());

        Optional<TrainerMonthlyWorkload> found =
                monthlyRepository.findByTrainerAndYearAndMonth(trainerModel, 2026, 3);

        assertTrue(found.isPresent());
        assertEquals(2026, found.get().getYear());
        assertEquals(3, found.get().getMonth());
        assertEquals(300, found.get().getTotalDuration());
        assertNotNull(found.get().getTrainer());
        assertEquals(trainerDao.getTrainerId().toString(), found.get().getTrainer().getTrainerId());
    }

    @Test
    void findByTrainerAndYearAndMonth_returnsEmpty_whenNotExists() {
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setUsername("trainer.three");
        trainerDao.setFirstName("Trainer");
        trainerDao.setLastName("Three");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);
        trainerDao = trainerJpaRepository.saveAndFlush(trainerDao);

        // note: no monthly rows inserted

        TrainerSummary trainerModel = new TrainerSummary();
        trainerModel.setTrainerId(trainerDao.getTrainerId().toString());
        trainerModel.setUsername(trainerDao.getUsername());

        Optional<TrainerMonthlyWorkload> found =
                monthlyRepository.findByTrainerAndYearAndMonth(trainerModel, 2026, 1);

        assertTrue(found.isEmpty());
    }

    @Test
    void save_persistsMonthly_andReturnsMappedModel() {
        // create trainer in DB first (so FK relation is valid)
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setUsername("trainer.save");
        trainerDao.setFirstName("Save");
        trainerDao.setLastName("Trainer");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);
        trainerDao = trainerJpaRepository.saveAndFlush(trainerDao);

        TrainerSummary trainerModel = new TrainerSummary();
        trainerModel.setTrainerId(trainerDao.getTrainerId().toString());
        trainerModel.setUsername(trainerDao.getUsername());
        trainerModel.setFirstName(trainerDao.getFirstName());
        trainerModel.setLastName(trainerDao.getLastName());
        trainerModel.setStatus(trainerDao.getStatus());
        trainerModel.setActive(trainerDao.getActive());

        TrainerMonthlyWorkload input = new TrainerMonthlyWorkload();
        input.setTrainer(trainerModel);
        input.setYear(2026);
        input.setMonth(4);
        input.setTotalDuration(55);

        TrainerMonthlyWorkload saved = monthlyRepository.save(input);

        assertNotNull(saved);
        // id can be null depending on your mapper/model; if your model has tmwId, assert it
        // assertNotNull(saved.getTmwId());
        assertEquals(2026, saved.getYear());
        assertEquals(4, saved.getMonth());
        assertEquals(55, saved.getTotalDuration());
        assertNotNull(saved.getTrainer());
        assertEquals(trainerDao.getTrainerId().toString(), saved.getTrainer().getTrainerId());

        // verify DB state
        List<TrainerMonthlyWorkloadDao> all = monthlyComponent.findAll();
        assertEquals(1, all.size());
        assertEquals(2026, all.get(0).getYear());
        assertEquals(4, all.get(0).getMonth());
        assertEquals(55, all.get(0).getTotalDuration());
        assertEquals(trainerDao.getTrainerId(), all.get(0).getTrainer().getTrainerId());
    }
}

