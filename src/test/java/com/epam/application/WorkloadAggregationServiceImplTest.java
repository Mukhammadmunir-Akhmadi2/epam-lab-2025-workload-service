package com.epam.application;

import com.epam.application.repository.TrainerSummaryRepository;
import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.enums.ActionType;
import com.epam.model.TrainingMonthSummary;
import com.epam.model.TrainingYearSummary;
import com.epam.model.TrainerTrainingSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadAggregationServiceImplTest {

    @Mock
    TrainerSummaryRepository trainerRepository;

    @InjectMocks
    WorkloadAggregationServiceImpl service;

    @Captor
    ArgumentCaptor<TrainerTrainingSummary> trainerCaptor;

    @Test
    void applyEvent_trainerExists_monthExists_ADD_increasesTotal() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("john");
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 2, 10));
        when(req.getActionType()).thenReturn(ActionType.ADD);
        when(req.getTrainingDuration()).thenReturn(30);

        // lenient stubs (not called because trainer exists)
        lenient().when(req.getTrainerFirstName()).thenReturn("John");
        lenient().when(req.getTrainerLastName()).thenReturn("Doe");
        lenient().when(req.getIsActive()).thenReturn(true);

        // existing trainer with a year/month
        TrainerTrainingSummary existingTrainer = new TrainerTrainingSummary();
        existingTrainer.setUsername("john");
        existingTrainer.setFirstName("John");
        existingTrainer.setLastName("Doe");
        existingTrainer.setActive(true);

        TrainingYearSummary yearSummary = new TrainingYearSummary();
        yearSummary.setYear(2026);
        TrainingMonthSummary monthSummary = new TrainingMonthSummary();
        monthSummary.setMonth(2);
        monthSummary.setTrainingsSummaryDuration(100L);
        yearSummary.getMonths().add(monthSummary);
        existingTrainer.getYears().add(yearSummary);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(TrainerTrainingSummary.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        service.applyEvent(req);

        // then
        verify(trainerRepository).save(trainerCaptor.capture());
        TrainerTrainingSummary savedTrainer = trainerCaptor.getValue();

        assertEquals("john", savedTrainer.getUsername());
        assertEquals("John", savedTrainer.getFirstName());
        assertEquals("Doe", savedTrainer.getLastName());
        assertTrue(savedTrainer.getActive());

        TrainingYearSummary savedYear = savedTrainer.getYears().stream()
                .filter(y -> y.getYear() == 2026)
                .findFirst().orElseThrow();
        TrainingMonthSummary savedMonth = savedYear.getMonths().stream()
                .filter(m -> m.getMonth() == 2)
                .findFirst().orElseThrow();

        assertEquals(130L, savedMonth.getTrainingsSummaryDuration()); // 100 + 30
    }

    @Test
    void applyEvent_trainerMissing_createsTrainer_monthMissing_ADD_setsTotalFromZero() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("new.user");
        when(req.getTrainerFirstName()).thenReturn("New");
        when(req.getTrainerLastName()).thenReturn("User");
        when(req.getIsActive()).thenReturn(false); // default false to avoid NPE
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 1, 5));
        when(req.getActionType()).thenReturn(ActionType.ADD);
        when(req.getTrainingDuration()).thenReturn(40);

        when(trainerRepository.findByUsername("new.user")).thenReturn(Optional.empty());
        when(trainerRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.applyEvent(req);

        // then
        verify(trainerRepository).save(trainerCaptor.capture());
        TrainerTrainingSummary savedTrainer = trainerCaptor.getValue();

        assertEquals("new.user", savedTrainer.getUsername());
        assertEquals("New", savedTrainer.getFirstName());
        assertEquals("User", savedTrainer.getLastName());
        assertFalse(savedTrainer.getActive());

        TrainingYearSummary savedYear = savedTrainer.getYears().stream()
                .filter(y -> y.getYear() == 2026)
                .findFirst().orElseThrow();
        TrainingMonthSummary savedMonth = savedYear.getMonths().stream()
                .filter(m -> m.getMonth() == 1)
                .findFirst().orElseThrow();

        assertEquals(40L, savedMonth.getTrainingsSummaryDuration());
    }

    @Test
    void applyEvent_SUBTRACT_belowZero_clampsToZero() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("john");
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(req.getActionType()).thenReturn(ActionType.DELETE);
        when(req.getTrainingDuration()).thenReturn(999);

        // lenient stubs
        lenient().when(req.getTrainerFirstName()).thenReturn("John");
        lenient().when(req.getTrainerLastName()).thenReturn("Doe");
        lenient().when(req.getIsActive()).thenReturn(true);

        TrainerTrainingSummary existingTrainer = new TrainerTrainingSummary();
        existingTrainer.setUsername("john");
        existingTrainer.setFirstName("John");
        existingTrainer.setLastName("Doe");
        existingTrainer.setActive(true);

        TrainingYearSummary yearSummary = new TrainingYearSummary();
        yearSummary.setYear(2026);
        TrainingMonthSummary monthSummary = new TrainingMonthSummary();
        monthSummary.setMonth(3);
        monthSummary.setTrainingsSummaryDuration(10L);
        yearSummary.getMonths().add(monthSummary);
        existingTrainer.getYears().add(yearSummary);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(TrainerTrainingSummary.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.applyEvent(req);

        // then
        verify(trainerRepository).save(trainerCaptor.capture());
        TrainerTrainingSummary savedTrainer = trainerCaptor.getValue();

        TrainingYearSummary savedYear = savedTrainer.getYears().stream()
                .filter(y -> y.getYear() == 2026)
                .findFirst().orElseThrow();
        TrainingMonthSummary savedMonth = savedYear.getMonths().stream()
                .filter(m -> m.getMonth() == 3)
                .findFirst().orElseThrow();

        assertEquals(0L, savedMonth.getTrainingsSummaryDuration());
    }
}