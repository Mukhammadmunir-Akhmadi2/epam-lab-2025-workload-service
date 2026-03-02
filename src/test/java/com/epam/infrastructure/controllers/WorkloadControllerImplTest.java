package com.epam.infrastructure.controllers;
import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.controllers.impl.WorkloadControllerImpl;
import com.epam.infrastructure.dtos.TrainerTrainingSummaryResponseDto;
import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
import com.epam.infrastructure.security.filters.JwtClaimsFilter;
import com.epam.model.TrainerTrainingSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkloadControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkloadControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadQueryServiceImpl queryService; // already mocked

    @MockitoBean
    private TrainerTrainingSummaryMapper trainerSummaryMapper; // <--- add this

    @MockitoBean
    private JwtClaimsFilter jwtFilter; // already mocked

    @Test
    void getTrainerWorkloadSummary_returns200_andBody() throws Exception {
        TrainerTrainingSummary model = new TrainerTrainingSummary();
        model.setUsername("john.smith");
        model.setFirstName("John");
        model.setLastName("Smith");
        model.setStatus(true);
        model.setYears(Set.of());

        when(queryService.getSummary("john.smith")).thenReturn(model);

        // map model to DTO
        var dto = mock(TrainerTrainingSummaryResponseDto.class);
        when(trainerSummaryMapper.toDto(model)).thenReturn(dto);

        mockMvc.perform(get("/trainers/{username}/workload", "john.smith")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(queryService).getSummary("john.smith");
        verify(trainerSummaryMapper).toDto(model);
        verifyNoMoreInteractions(queryService, trainerSummaryMapper);
    }

    @Test
    void getTrainerWorkloadSummary_returns404_whenTrainerNotFound() throws Exception {
        when(queryService.getSummary("missing.user"))
                .thenThrow(new ResourceNotFoundException("Trainer not found: missing.user"));

        mockMvc.perform(get("/trainers/{username}/workload", "missing.user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(queryService).getSummary("missing.user");
        verifyNoMoreInteractions(queryService);
    }
}