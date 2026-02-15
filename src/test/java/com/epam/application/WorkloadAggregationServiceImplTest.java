package com.epam.application;

import com.epam.application.repository.TrainerMonthlyWorkloadRepository;
import com.epam.application.repository.TrainerRepository;
import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.enums.ActionType;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadAggregationServiceImplTest {

    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainerMonthlyWorkloadRepository monthlyRepository;

    @InjectMocks
    WorkloadAggregationServiceImpl service;

    @Captor
    ArgumentCaptor<TrainerSummary> trainerCaptor;
    @Captor ArgumentCaptor<TrainerMonthlyWorkload> monthlyCaptor;

    @Test
    void applyEvent_trainerExists_monthExists_ADD_increasesTotal() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("john");
        when(req.getTrainerFirstName()).thenReturn("John");
        when(req.getTrainerLastName()).thenReturn("Doe");
        when(req.getIsActive()).thenReturn(true);
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 2, 10));
        when(req.getActionType()).thenReturn(ActionType.ADD);
        when(req.getTrainingDuration()).thenReturn(30);

        TrainerSummary existingTrainer = new TrainerSummary();
        existingTrainer.setUsername("john");

        TrainerSummary savedTrainer = new TrainerSummary();
        savedTrainer.setUsername("john");
        savedTrainer.setTrainerId("t-1"); // if your model has it; not required
        savedTrainer.setFirstName("John");
        savedTrainer.setLastName("Doe");
        savedTrainer.setActive(true);

        TrainerMonthlyWorkload existingMonthly = new TrainerMonthlyWorkload();
        existingMonthly.setTrainer(savedTrainer);
        existingMonthly.setYear(2026);
        existingMonthly.setMonth(2);
        existingMonthly.setTotalDuration(100);

        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(TrainerSummary.class))).thenReturn(savedTrainer);
        when(monthlyRepository.findByTrainerAndYearAndMonth(savedTrainer, 2026, 2))
                .thenReturn(Optional.of(existingMonthly));

        // when
        service.applyEvent(req);

        // then: trainer saved with updated fields
        verify(trainerRepository).save(trainerCaptor.capture());
        TrainerSummary trainerToSave = trainerCaptor.getValue();
        assertEquals("john", trainerToSave.getUsername());
        assertEquals("John", trainerToSave.getFirstName());
        assertEquals("Doe", trainerToSave.getLastName());
        assertTrue(trainerToSave.getActive());

        // then: monthly saved with increased total
        verify(monthlyRepository).save(monthlyCaptor.capture());
        TrainerMonthlyWorkload monthlyToSave = monthlyCaptor.getValue();
        assertEquals(2026, monthlyToSave.getYear());
        assertEquals(2, monthlyToSave.getMonth());
        assertEquals(130, monthlyToSave.getTotalDuration());
        assertNotNull(monthlyToSave.getTrainer());
        assertEquals("john", monthlyToSave.getTrainer().getUsername());

        verify(trainerRepository).findByUsername("john");
        verify(monthlyRepository).findByTrainerAndYearAndMonth(savedTrainer, 2026, 2);
        verifyNoMoreInteractions(trainerRepository, monthlyRepository);
    }

    @Test
    void applyEvent_trainerMissing_createsTrainer_monthMissing_createsMonthly_ADD_setsTotalFromZero() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("new.user");
        when(req.getTrainerFirstName()).thenReturn("New");
        when(req.getTrainerLastName()).thenReturn("User");
        when(req.getIsActive()).thenReturn(null); // should become false (Boolean.TRUE.equals(null) => false)
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 1, 5));
        when(req.getActionType()).thenReturn(ActionType.ADD);
        when(req.getTrainingDuration()).thenReturn(40);

        when(trainerRepository.findByUsername("new.user")).thenReturn(Optional.empty());

        TrainerSummary savedTrainer = new TrainerSummary();
        savedTrainer.setUsername("new.user");
        savedTrainer.setTrainerId("t-2");
        when(trainerRepository.save(any(TrainerSummary.class))).thenReturn(savedTrainer);

        when(monthlyRepository.findByTrainerAndYearAndMonth(savedTrainer, 2026, 1))
                .thenReturn(Optional.empty());

        // make monthlyRepository.save return same instance (common pattern)
        when(monthlyRepository.save(any(TrainerMonthlyWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.applyEvent(req);

        // then: trainer created and saved with fields applied
        verify(trainerRepository).save(trainerCaptor.capture());
        TrainerSummary trainerToSave = trainerCaptor.getValue();
        assertEquals("new.user", trainerToSave.getUsername());
        assertEquals("New", trainerToSave.getFirstName());
        assertEquals("User", trainerToSave.getLastName());
        assertFalse(trainerToSave.getActive()); // because req.getIsActive() was null

        // then: monthly created (from zero) and saved with total = 40
        verify(monthlyRepository).save(monthlyCaptor.capture());
        TrainerMonthlyWorkload monthlyToSave = monthlyCaptor.getValue();
        assertEquals(savedTrainer, monthlyToSave.getTrainer());
        assertEquals(2026, monthlyToSave.getYear());
        assertEquals(1, monthlyToSave.getMonth());
        assertEquals(40, monthlyToSave.getTotalDuration());

        verify(trainerRepository).findByUsername("new.user");
        verify(monthlyRepository).findByTrainerAndYearAndMonth(savedTrainer, 2026, 1);
        verifyNoMoreInteractions(trainerRepository, monthlyRepository);
    }

    @Test
    void applyEvent_SUBTRACT_belowZero_clampsToZero() {
        // given
        TrainerWorkloadRequestDto req = mock(TrainerWorkloadRequestDto.class);
        when(req.getTrainerUsername()).thenReturn("john");
        when(req.getTrainerFirstName()).thenReturn("John");
        when(req.getTrainerLastName()).thenReturn("Doe");
        when(req.getIsActive()).thenReturn(true);
        when(req.getTrainingDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(req.getActionType()).thenReturn(ActionType.DELETE); // any non-ADD becomes subtract in your code
        when(req.getTrainingDuration()).thenReturn(999);

        TrainerSummary existingTrainer = new TrainerSummary();
        existingTrainer.setUsername("john");

        TrainerSummary savedTrainer = new TrainerSummary();
        savedTrainer.setUsername("john");
        when(trainerRepository.findByUsername("john")).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(TrainerSummary.class))).thenReturn(savedTrainer);

        TrainerMonthlyWorkload existingMonthly = new TrainerMonthlyWorkload();
        existingMonthly.setTrainer(savedTrainer);
        existingMonthly.setYear(2026);
        existingMonthly.setMonth(3);
        existingMonthly.setTotalDuration(10);

        when(monthlyRepository.findByTrainerAndYearAndMonth(savedTrainer, 2026, 3))
                .thenReturn(Optional.of(existingMonthly));

        // when
        service.applyEvent(req);

        // then
        verify(monthlyRepository).save(monthlyCaptor.capture());
        assertEquals(0, monthlyCaptor.getValue().getTotalDuration());

        verifyNoMoreInteractions(trainerRepository, monthlyRepository);
    }
}