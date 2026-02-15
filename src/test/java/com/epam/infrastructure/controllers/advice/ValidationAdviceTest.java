package com.epam.infrastructure.controllers.advice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ValidationAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "system", authorities = "SYSTEM")
    void workloadEvents_invalidDto_shouldReturn400ProblemDetail() throws Exception {
        // invalid: blanks + nulls + duration < 1
        String invalidBody = """
            {
              "trainerUsername": "",
              "trainerFirstName": "",
              "trainerLastName": "",
              "isActive": null,
              "trainingDate": null,
              "trainingDuration": 0,
              "actionType": null
            }
            """;

        mockMvc.perform(post("/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                // default ProblemDetail fields
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                // your advice concatenates field errors into "detail"
                .andExpect(jsonPath("$.detail").isString())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("trainerUsername"),
                        org.hamcrest.Matchers.containsString("trainerFirstName"),
                        org.hamcrest.Matchers.containsString("trainerLastName"),
                        org.hamcrest.Matchers.containsString("isActive"),
                        org.hamcrest.Matchers.containsString("trainingDate"),
                        org.hamcrest.Matchers.containsString("trainingDuration"),
                        org.hamcrest.Matchers.containsString("actionType")
                )));
    }

    @Test
    @WithMockUser(username = "system", authorities = "SYSTEM")
    void workloadEvents_missingBody_shouldReturn400MissingRequestBodyProblemDetail() throws Exception {
        mockMvc.perform(post("/workload-events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Missing Request Body"))
                .andExpect(jsonPath("$.detail").value("Request body is missing or invalid JSON"));
    }

    @Test
    @WithMockUser(username = "system", authorities = "SYSTEM")
    void workloadEvents_invalidJson_shouldReturn400MissingRequestBodyProblemDetail() throws Exception {
        mockMvc.perform(post("/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Missing Request Body"))
                .andExpect(jsonPath("$.detail").value("Request body is missing or invalid JSON"));
    }

}
