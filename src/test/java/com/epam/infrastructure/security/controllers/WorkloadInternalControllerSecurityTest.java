package com.epam.infrastructure.security.controllers;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.mappers.TrainerTrainingSummaryMapper;
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

    @MockitoBean
    private TrainerTrainingSummaryMapper trainerSummaryMapper;

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
}
