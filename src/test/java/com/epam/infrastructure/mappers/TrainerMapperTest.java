package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.infrastructure.enums.TrainerStatus;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrainerMapperTest {

    private final TrainerMapper mapper = Mappers.getMapper(TrainerMapper.class);

    @Test
    void toModel_mapsAllFields_andConvertsUuidToString() {
        UUID id = UUID.randomUUID();

        TrainerSummaryDao dao = new TrainerSummaryDao();
        dao.setTrainerId(id);
        dao.setUsername("john");
        dao.setFirstName("John");
        dao.setLastName("Doe");
        dao.setStatus(TrainerStatus.ACTIVE);   // adjust enum value if different
        dao.setActive(true);

        TrainerSummary model = mapper.toModel(dao);

        assertNotNull(model);
        assertEquals(id.toString(), model.getTrainerId());
        assertEquals("john", model.getUsername());
        assertEquals("John", model.getFirstName());
        assertEquals("Doe", model.getLastName());
        assertEquals(TrainerStatus.ACTIVE, model.getStatus());
        assertTrue(model.getActive());
    }

    @Test
    void toModel_withNullId_setsNullId() {
        TrainerSummaryDao dao = new TrainerSummaryDao();
        dao.setTrainerId(null);
        dao.setUsername("john");
        dao.setStatus(TrainerStatus.ACTIVE);
        dao.setActive(true);

        TrainerSummary model = mapper.toModel(dao);

        assertNotNull(model);
        assertNull(model.getTrainerId());
        assertEquals("john", model.getUsername());
    }

    @Test
    void toDao_mapsAllFields_andConvertsStringToUuid() {
        UUID id = UUID.randomUUID();

        TrainerSummary model = new TrainerSummary();
        model.setTrainerId(id.toString());
        model.setUsername("john");
        model.setFirstName("John");
        model.setLastName("Doe");
        model.setStatus(TrainerStatus.ACTIVE);
        model.setActive(true);

        TrainerSummaryDao dao = mapper.toDao(model);

        assertNotNull(dao);
        assertEquals(id, dao.getTrainerId());
        assertEquals("john", dao.getUsername());
        assertEquals("John", dao.getFirstName());
        assertEquals("Doe", dao.getLastName());
        assertEquals(TrainerStatus.ACTIVE, dao.getStatus());
        assertTrue(dao.getActive());
    }

    @Test
    void toDao_withNullId_setsNullId() {
        TrainerSummary model = new TrainerSummary();
        model.setTrainerId(null);
        model.setUsername("john");
        model.setStatus(TrainerStatus.ACTIVE);
        model.setActive(true);

        TrainerSummaryDao dao = mapper.toDao(model);

        assertNotNull(dao);
        assertNull(dao.getTrainerId());
    }
}
