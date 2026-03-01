package com.epam.application;

import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.model.TrainingMonthSummary;
import com.epam.model.TrainingYearSummary;
import com.epam.model.TrainerTrainingSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadQueryServiceImplTest {

    @Mock
    TrainerSummaryRepository trainerRepository;

    @InjectMocks
    WorkloadQueryServiceImpl service;

    @Test
    void getSummary_throws_whenTrainerNotFound() {
        when(trainerRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.getSummary("missing"));

        assertTrue(ex.getMessage().contains("Trainer not found: missing"));

        verify(trainerRepository).findByUsername("missing");
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void getSummary_returnsTrainerWithYearsAndMonths() {
        // given trainer with nested years/months
        TrainerTrainingSummary trainer = new TrainerTrainingSummary();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setStatus(true);
        trainer.setActive(true);

        TrainingYearSummary y2026 = new TrainingYearSummary();
        y2026.setYear(2026);
        y2026.setMonths(new HashSet<>(Set.of(
                month(2, 90),
                month(1, 120)
        )));

        TrainingYearSummary y2025 = new TrainingYearSummary();
        y2025.setYear(2025);
        y2025.setMonths(new HashSet<>(Set.of(
                month(12, 30),
                month(1, 10)
        )));

        trainer.setYears(new HashSet<>(Set.of(y2026, y2025)));

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));

        // when
        TrainerTrainingSummary summary = service.getSummary("john");

        // then: basic trainer info
        assertEquals("john", summary.getUsername());
        assertEquals("John", summary.getFirstName());
        assertEquals("Doe", summary.getLastName());
        assertTrue(summary.getStatus());

        // then: years exist
        assertNotNull(summary.getYears());
        assertEquals(2, summary.getYears().size());

        // find year 2025 and 2026
        TrainingYearSummary year2025 = summary.getYears().stream()
                .filter(y -> y.getYear() == 2025)
                .findFirst()
                .orElseThrow();
        TrainingYearSummary year2026 = summary.getYears().stream()
                .filter(y -> y.getYear() == 2026)
                .findFirst()
                .orElseThrow();

        // 2025 months
        assertEquals(2, year2025.getMonths().size());
        assertTrue(year2025.getMonths().stream().anyMatch(m -> m.getMonth() == 1 && m.getTrainingsSummaryDuration() == 10));
        assertTrue(year2025.getMonths().stream().anyMatch(m -> m.getMonth() == 12 && m.getTrainingsSummaryDuration() == 30));

        // 2026 months
        assertEquals(2, year2026.getMonths().size());
        assertTrue(year2026.getMonths().stream().anyMatch(m -> m.getMonth() == 1 && m.getTrainingsSummaryDuration() == 120));
        assertTrue(year2026.getMonths().stream().anyMatch(m -> m.getMonth() == 2 && m.getTrainingsSummaryDuration() == 90));

        verify(trainerRepository).findByUsername("john");
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void getSummary_returnsEmptyYears_whenNoYears() {
        TrainerTrainingSummary trainer = new TrainerTrainingSummary();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setStatus(false);
        trainer.setActive(true);
        trainer.setYears(new HashSet<>());

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));

        TrainerTrainingSummary summary = service.getSummary("john");

        assertEquals("john", summary.getUsername());
        assertEquals("John", summary.getFirstName());
        assertEquals("Doe", summary.getLastName());
        assertFalse(summary.getStatus());
        assertNotNull(summary.getYears());
        assertTrue(summary.getYears().isEmpty());

        verify(trainerRepository).findByUsername("john");
        verifyNoMoreInteractions(trainerRepository);
    }

    private static TrainingMonthSummary month(int month, long duration) {
        TrainingMonthSummary m = new TrainingMonthSummary();
        m.setMonth(month);
        m.setTrainingsSummaryDuration(duration);
        return m;
    }
}