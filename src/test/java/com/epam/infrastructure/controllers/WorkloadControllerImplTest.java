package com.epam.infrastructure.controllers;
import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.controllers.impl.WorkloadControllerImpl;
import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;
import com.epam.infrastructure.security.filters.JwtClaimsFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkloadControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkloadControllerImplTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WorkloadQueryServiceImpl queryService;

    @MockitoBean
    private JwtClaimsFilter jwtFilter;

    @Test
    void getTrainerWorkloadSummary_returns200_andBody() throws Exception {
        // given
        TrainerMonthlySummaryResponseDto dto = new TrainerMonthlySummaryResponseDto();
        dto.setTrainerUsername("john.smith");
        dto.setTrainerFirstName("John");
        dto.setTrainerLastName("Smith");
        dto.setTrainerStatus(true);
        dto.setYears(java.util.List.of()); // keep simple

        when(queryService.getSummary("john.smith")).thenReturn(dto);

        // when/then
        mockMvc.perform(get("/trainers/{username}/workload", "john.smith")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trainerUsername").value("john.smith"))
                .andExpect(jsonPath("$.trainerFirstName").value("John"))
                .andExpect(jsonPath("$.trainerLastName").value("Smith"))
                .andExpect(jsonPath("$.trainerStatus").value(true))
                .andExpect(jsonPath("$.years").isArray());

        verify(queryService).getSummary("john.smith");
        verifyNoMoreInteractions(queryService);
    }

    @Test
    void getTrainerWorkloadSummary_returns404_whenTrainerNotFound() throws Exception {
        // given
        when(queryService.getSummary("missing.user"))
                .thenThrow(new ResourceNotFoundException("Trainer not found: missing.user"));

        // when/then
        mockMvc.perform(get("/trainers/{username}/workload", "missing.user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(queryService).getSummary("missing.user");
        verifyNoMoreInteractions(queryService);
    }
}