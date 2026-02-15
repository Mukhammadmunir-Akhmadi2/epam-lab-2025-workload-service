package com.epam.application;
import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.repository.TrainerMonthlyWorkloadRepository;
import com.epam.application.repository.TrainerRepository;
import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadQueryServiceImplTest {

    @Mock TrainerRepository trainerRepository;
    @Mock TrainerMonthlyWorkloadRepository monthlyRepository;

    @InjectMocks
    WorkloadQueryServiceImpl service;

    @Test
    void getSummary_throws_whenTrainerNotFound() {
        when(trainerRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.getSummary("missing"));

        assertTrue(ex.getMessage().contains("Trainer not found: missing"));

        verify(trainerRepository).findByUsername("missing");
        verifyNoInteractions(monthlyRepository);
        verifyNoMoreInteractions(trainerRepository);
    }

    @Test
    void getSummary_buildsGroupedSortedResponse() {
        // given trainer
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));

        // workloads intentionally unsorted + mixed years
        TrainerMonthlyWorkload r1 = workload(2026, 2, 90);
        TrainerMonthlyWorkload r2 = workload(2025, 12, 30);
        TrainerMonthlyWorkload r3 = workload(2026, 1, 120);
        TrainerMonthlyWorkload r4 = workload(2025, 1, 10);

        when(monthlyRepository.findAllByTrainer(trainer)).thenReturn(List.of(r1, r2, r3, r4));

        // when
        TrainerMonthlySummaryResponseDto resp = service.getSummary("john");

        // then: trainer fields copied
        assertEquals("john", resp.getTrainerUsername());
        assertEquals("John", resp.getTrainerFirstName());
        assertEquals("Doe", resp.getTrainerLastName());
        assertTrue(resp.isTrainerStatus());

        // then: years sorted ascending: 2025 then 2026
        assertNotNull(resp.getYears());
        assertEquals(2, resp.getYears().size());

        assertEquals(2025, resp.getYears().get(0).getYear());
        assertEquals(2026, resp.getYears().get(1).getYear());

        // months sorted inside each year
        // 2025: month 1 then 12
        assertEquals(2, resp.getYears().get(0).getMonths().size());
        assertEquals(1, resp.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(10, resp.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
        assertEquals(12, resp.getYears().get(0).getMonths().get(1).getMonth());
        assertEquals(30, resp.getYears().get(0).getMonths().get(1).getTrainingSummaryDuration());

        // 2026: month 1 then 2
        assertEquals(2, resp.getYears().get(1).getMonths().size());
        assertEquals(1, resp.getYears().get(1).getMonths().get(0).getMonth());
        assertEquals(120, resp.getYears().get(1).getMonths().get(0).getTrainingSummaryDuration());
        assertEquals(2, resp.getYears().get(1).getMonths().get(1).getMonth());
        assertEquals(90, resp.getYears().get(1).getMonths().get(1).getTrainingSummaryDuration());

        verify(trainerRepository).findByUsername("john");
        verify(monthlyRepository).findAllByTrainer(trainer);
        verifyNoMoreInteractions(trainerRepository, monthlyRepository);
    }

    @Test
    void getSummary_returnsEmptyYears_whenNoWorkloads() {
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername("john");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(false);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(trainer));
        when(monthlyRepository.findAllByTrainer(trainer)).thenReturn(List.of());

        TrainerMonthlySummaryResponseDto resp = service.getSummary("john");

        assertEquals("john", resp.getTrainerUsername());
        assertEquals("John", resp.getTrainerFirstName());
        assertEquals("Doe", resp.getTrainerLastName());
        assertFalse(resp.isTrainerStatus());

        assertNotNull(resp.getYears());
        assertTrue(resp.getYears().isEmpty());

        verify(trainerRepository).findByUsername("john");
        verify(monthlyRepository).findAllByTrainer(trainer);
        verifyNoMoreInteractions(trainerRepository, monthlyRepository);
    }

    private static TrainerMonthlyWorkload workload(int year, int month, int totalDuration) {
        TrainerMonthlyWorkload w = new TrainerMonthlyWorkload();
        w.setYear(year);
        w.setMonth(month);
        w.setTotalDuration(totalDuration);
        return w;
    }
}