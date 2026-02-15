package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerMonthlyWorkloadDao;
import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class TrainerMonthlyWorkloadMapperTest {

    @Autowired
    private TrainerMonthlyWorkloadMapper trainerMonthlyWorkloadMapper;

    @Test
    void toModel_mapsAllFields_includingNestedTrainer_andConvertsTmwId() {
        UUID tmwId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setTrainerId(trainerId);
        trainerDao.setUsername("john");
        trainerDao.setFirstName("John");
        trainerDao.setLastName("Doe");
        trainerDao.setStatus(TrainerStatus.ACTIVE); // adjust if needed
        trainerDao.setActive(true);

        TrainerMonthlyWorkloadDao dao = new TrainerMonthlyWorkloadDao();
        dao.setTmwId(tmwId);
        dao.setTrainer(trainerDao);
        dao.setYear(2026);
        dao.setMonth(2);
        dao.setTotalDuration(120);

        TrainerMonthlyWorkload model = trainerMonthlyWorkloadMapper.toModel(dao);

        assertNotNull(model);
        assertEquals(tmwId.toString(), model.getTmwId());
        assertEquals(2026, model.getYear());
        assertEquals(2, model.getMonth());
        assertEquals(120, model.getTotalDuration());

        assertNotNull(model.getTrainer());
        assertEquals(trainerId.toString(), model.getTrainer().getTrainerId());
        assertEquals("john", model.getTrainer().getUsername());
        assertEquals("John", model.getTrainer().getFirstName());
        assertEquals("Doe", model.getTrainer().getLastName());
        assertEquals(TrainerStatus.ACTIVE, model.getTrainer().getStatus());
        assertTrue(model.getTrainer().getActive());
    }

    @Test
    void toDao_mapsAllFields_includingNestedTrainer_andConvertsTmwId() {
        UUID tmwId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();

        TrainerSummary trainer = new TrainerSummary();
        trainer.setTrainerId(trainerId.toString());
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setStatus(TrainerStatus.ACTIVE); // adjust if needed
        trainer.setActive(true);

        TrainerMonthlyWorkload model = new TrainerMonthlyWorkload();
        model.setTmwId(tmwId.toString());
        model.setTrainer(trainer);
        model.setYear(2026);
        model.setMonth(2);
        model.setTotalDuration(120);

        TrainerMonthlyWorkloadDao dao = trainerMonthlyWorkloadMapper.toDao(model);

        assertNotNull(dao);
        assertEquals(tmwId, dao.getTmwId());
        assertEquals(2026, dao.getYear());
        assertEquals(2, dao.getMonth());
        assertEquals(120, dao.getTotalDuration());

        assertNotNull(dao.getTrainer());
        assertEquals(trainerId, dao.getTrainer().getTrainerId());
        assertEquals("john", dao.getTrainer().getUsername());
        assertEquals("John", dao.getTrainer().getFirstName());
        assertEquals("Doe", dao.getTrainer().getLastName());
        assertEquals(TrainerStatus.ACTIVE, dao.getTrainer().getStatus());
        assertTrue(dao.getTrainer().getActive());
    }

    @Test
    void toModel_withNullIds_setsNullStrings() {
        TrainerSummaryDao trainerDao = new TrainerSummaryDao();
        trainerDao.setTrainerId(null);
        trainerDao.setUsername("john");
        trainerDao.setStatus(TrainerStatus.ACTIVE);
        trainerDao.setActive(true);

        TrainerMonthlyWorkloadDao dao = new TrainerMonthlyWorkloadDao();
        dao.setTmwId(null);
        dao.setTrainer(trainerDao);
        dao.setYear(2026);
        dao.setMonth(2);
        dao.setTotalDuration(120);

        TrainerMonthlyWorkload model = trainerMonthlyWorkloadMapper.toModel(dao);

        assertNotNull(model);
        assertNull(model.getTmwId());
        assertNotNull(model.getTrainer());
        assertNull(model.getTrainer().getTrainerId());
    }
}
