package com.epam.infrastructure.security.controllers;

import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.services.impl.WorkloadQueryServiceImpl;
import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkloadControllerSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private WorkloadQueryServiceImpl queryService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getWorkload_withoutAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/trainers/john/workload"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "user", authorities = "TRAINER")
    void getWorkload_withNonAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/trainers/john/workload"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getWorkload_withAdmin_shouldReturnOk() throws Exception {
        TrainerMonthlySummaryResponseDto dto = new TrainerMonthlySummaryResponseDto();
        dto.setTrainerUsername("john");
        dto.setTrainerFirstName("John");
        dto.setTrainerLastName("Doe");
        dto.setTrainerStatus(true);
        dto.setYears(java.util.List.of());

        when(queryService.getSummary("john")).thenReturn(dto);

        mockMvc.perform(get("/trainers/john/workload"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.trainerUsername").value("john"))
                .andExpect(jsonPath("$.trainerFirstName").value("John"))
                .andExpect(jsonPath("$.trainerLastName").value("Doe"))
                .andExpect(jsonPath("$.trainerStatus").value(true))
                .andExpect(jsonPath("$.years").isArray());

        verify(queryService).getSummary("john");
        verifyNoMoreInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getWorkload_withAdmin_butTrainerNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Trainer not found: missing"))
                .when(queryService).getSummary("missing");

        mockMvc.perform(get("/trainers/missing/workload"))
                .andExpect(status().isNotFound());

        verify(queryService).getSummary("missing");
        verifyNoMoreInteractions(queryService);
    }
}
