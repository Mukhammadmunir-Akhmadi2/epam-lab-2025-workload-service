package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerTrainingSummaryDao;
import com.epam.infrastructure.daos.TrainingMonthSummaryDao;
import com.epam.infrastructure.daos.TrainingYearSummaryDao;
import com.epam.infrastructure.dtos.TrainerTrainingSummaryResponseDto;
import com.epam.model.TrainerTrainingSummary;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTrainingSummaryMapperTest {

    private static TrainerTrainingSummaryMapper mapper;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        mapper = Mappers.getMapper(TrainerTrainingSummaryMapper.class);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void daoToModel_mappingWorks_correctly() {
        // given
        TrainerTrainingSummaryDao dao = new TrainerTrainingSummaryDao();
        dao.setTrainerId(UUID.randomUUID().toString());
        dao.setUsername("john.doe");
        dao.setFirstName("John");
        dao.setLastName("Doe");
        dao.setStatus(true);
        dao.setActive(true);

        TrainingMonthSummaryDao m1 = new TrainingMonthSummaryDao();
        m1.setMonth(2);
        m1.setTrainingsSummaryDuration(120L);

        TrainingMonthSummaryDao m2 = new TrainingMonthSummaryDao();
        m2.setMonth(1);
        m2.setTrainingsSummaryDuration(100L);

        TrainingYearSummaryDao y2026 = new TrainingYearSummaryDao();
        y2026.setYear(2026);
        y2026.setMonths(Set.of(m1, m2));

        dao.setYears(Set.of(y2026));

        // when
        TrainerTrainingSummary model = mapper.toModel(dao);

        // then: basic fields
        assertEquals(dao.getUsername(), model.getUsername());
        assertEquals(dao.getFirstName(), model.getFirstName());
        assertEquals(dao.getLastName(), model.getLastName());
        assertEquals(dao.getStatus(), model.getStatus());
        assertEquals(dao.getActive(), model.getActive());
        assertNotNull(model.getYears());
        assertEquals(1, model.getYears().size());

        // then: validate constraints
        Set<ConstraintViolation<TrainerTrainingSummary>> violations = validator.validate(model);
        assertTrue(violations.isEmpty(), "All fields should be valid and not null");
    }

    @Test
    void modelToDto_mappingAndSortingWorks() {
        // given
        TrainerTrainingSummary model = new TrainerTrainingSummary();
        model.setTrainerId(UUID.randomUUID().toString());
        model.setUsername("jane.doe");
        model.setFirstName("Jane");
        model.setLastName("Doe");
        model.setStatus(true);
        model.setActive(true);

        com.epam.model.TrainingMonthSummary m1 = new com.epam.model.TrainingMonthSummary();
        m1.setMonth(12);
        m1.setTrainingsSummaryDuration(50L);

        com.epam.model.TrainingMonthSummary m2 = new com.epam.model.TrainingMonthSummary();
        m2.setMonth(1);
        m2.setTrainingsSummaryDuration(100L);

        com.epam.model.TrainingYearSummary y2025 = new com.epam.model.TrainingYearSummary();
        y2025.setYear(2025);
        y2025.setMonths(new HashSet<>(Set.of(m1, m2)));

        model.setYears(new HashSet<>(Set.of(y2025)));

        // when
        TrainerTrainingSummaryResponseDto dto = mapper.toDto(model);

        // then: basic fields
        assertEquals(model.getUsername(), dto.getUsername());
        assertEquals(model.getFirstName(), dto.getFirstName());
        assertEquals(model.getLastName(), dto.getLastName());
        assertEquals(model.getStatus(), dto.getStatus());

        // then: years sorted ascending
        assertNotNull(dto.getYears());
        assertEquals(1, dto.getYears().size());
        assertEquals(2025, dto.getYears().get(0).getYear());

        // months sorted ascending
        assertEquals(2, dto.getYears().get(0).getMonths().size());
        assertEquals(1, dto.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(100, dto.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
        assertEquals(12, dto.getYears().get(0).getMonths().get(1).getMonth());
        assertEquals(50, dto.getYears().get(0).getMonths().get(1).getTrainingsSummaryDuration());
    }

    @Test
    void roundTrip_daoModelDto_backWorks() {
        TrainerTrainingSummaryDao dao = new TrainerTrainingSummaryDao();
        dao.setTrainerId(UUID.randomUUID().toString());
        dao.setUsername("round.trip");
        dao.setFirstName("Round");
        dao.setLastName("Trip");
        dao.setStatus(true);
        dao.setActive(true);

        TrainingMonthSummaryDao m = new TrainingMonthSummaryDao();
        m.setMonth(1);
        m.setTrainingsSummaryDuration(200L);

        TrainingYearSummaryDao y = new TrainingYearSummaryDao();
        y.setYear(2026);
        y.setMonths(Set.of(m));

        dao.setYears(Set.of(y));

        TrainerTrainingSummary model = mapper.toModel(dao);
        TrainerTrainingSummaryResponseDto dto = mapper.toDto(model);

        assertEquals(dao.getUsername(), model.getUsername());
        assertEquals(dao.getUsername(), dto.getUsername());
        assertNotNull(dto.getYears());
        assertEquals(1, dto.getYears().size());
        assertEquals(2026, dto.getYears().get(0).getYear());
        assertEquals(1, dto.getYears().get(0).getMonths().size());
        assertEquals(200, dto.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
    }
}