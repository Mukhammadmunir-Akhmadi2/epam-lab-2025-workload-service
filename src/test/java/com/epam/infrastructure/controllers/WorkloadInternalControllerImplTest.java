package com.epam.infrastructure.controllers;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.controllers.impl.WorkloadInternalControllerImpl;
import com.epam.infrastructure.security.filters.JwtClaimsFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkloadInternalControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkloadInternalControllerImplTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WorkloadAggregationServiceImpl service;

    @MockitoBean
    JwtClaimsFilter jwtFilter;

    @Test
    void acceptWorkloadEvent_returns200_andCallsService() throws Exception {
        // given: VALID request JSON (adjust fields to match TrainerWorkloadRequestDto)
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

        // when/then
        mockMvc.perform(post("/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(service, times(1)).applyEvent(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void acceptWorkloadEvent_returns400_whenRequestInvalid() throws Exception {
        String body = """
                {
                  "trainerUsername": "",
                  "trainingDuration": -1
                }
                """;

        mockMvc.perform(post("/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
