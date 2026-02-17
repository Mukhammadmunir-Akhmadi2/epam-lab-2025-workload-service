package com.epam.infrastructure.controllers.advice;

import com.epam.infrastructure.security.CustomAccessDeniedHandler;
import com.epam.infrastructure.security.JwtAuthenticationEntryPoint;
import com.epam.infrastructure.security.filters.JwtClaimsFilter;
import com.epam.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdviceTestController.class)
@AutoConfigureMockMvc(addFilters = false)   // ✅ disables Spring Security filters
@Import(GlobalControllerAdvice.class)
class GlobalControllerAdviceTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    JwtClaimsFilter jwtClaimsFilter;
    @MockitoBean
    JwtAuthenticationEntryPoint authEntrypoint;
    @MockitoBean
    CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void handleResourceNotFound_shouldReturn404ProblemDetail() throws Exception {
        mvc.perform(get("/__test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.detail", containsString("Trainer not found")));
    }

    @Test
    void handleIllegalArgument_shouldReturn400ProblemDetail() throws Exception {
        mvc.perform(get("/__test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Illegal Argument")))
                .andExpect(jsonPath("$.detail", containsString("bad arg")));
    }

    @Test
    void handleInvalidCredentials_shouldReturn401ProblemDetail() throws Exception {
        mvc.perform(get("/__test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Invalid Credentials")))
                .andExpect(jsonPath("$.detail", containsString("bad creds")));
    }

    @Test
    void handleConstraintViolation_shouldReturn400_withJoinedErrors() throws Exception {
        mvc.perform(get("/__test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.detail", is("username: must not be blank")));
    }

    @Test
    void handleGeneric_shouldReturn500ProblemDetail() throws Exception {
        mvc.perform(get("/__test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("Internal Server Error")))
                .andExpect(jsonPath("$.detail", containsString("boom")));
    }
}
