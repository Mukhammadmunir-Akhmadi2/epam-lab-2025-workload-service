package com.epam.infrastructure.security.controllers;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkloadInternalControllerSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private WorkloadAggregationServiceImpl workloadAggregationService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void acceptWorkloadEvent_withoutAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/workload-events")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(workloadAggregationService);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void acceptWorkloadEvent_withWrongRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/workload-events")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(workloadAggregationService);
    }

    @Test
    @WithMockUser(username = "system", authorities = "SYSTEM")
    void acceptWorkloadEvent_withSystemRole_shouldReturnOk_andCallService() throws Exception {
        // adjust fields to match your request DTO and @Valid constraints
        String body = """
                {
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "isActive": true,
                  "trainingDate": "2026-02-09",
                  "trainingDuration": 45,
                  "actionType": "ADD"
                }
                """;

        mockMvc.perform(post("/workload-events")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        verify(workloadAggregationService, times(1)).applyEvent(any());
        verifyNoMoreInteractions(workloadAggregationService);
    }
}
