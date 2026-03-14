package com.epam.cucumber.steps;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.dtos.TrainerTrainingSummaryResponseDto;
import com.epam.infrastructure.enums.ActionType;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.persistence.TrainerSummaryDocumentRepository;
import com.epam.model.TrainerTrainingSummary;
import com.epam.model.TrainingMonthSummary;
import com.epam.model.TrainingYearSummary;
import com.epam.application.repository.TrainerSummaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorkloadComponentSteps {

    @Autowired
    private WorkloadAggregationServiceImpl aggregationService;

    @Autowired
    private TrainerSummaryRepository trainerSummaryRepository;

    @Autowired
    private TrainerSummaryDocumentRepository documentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions lastResult;


    @Before
    public void cleanUp() {
        documentRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }


    private void authenticateAsSystem() {
        var auth = new UsernamePasswordAuthenticationToken(
                "system", null,
                List.of(new SimpleGrantedAuthority("SYSTEM"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Given("no trainer summary exists for {string}")
    public void noTrainerSummaryExists(String username) {
        documentRepository.findByUsername(username)
                .ifPresent(t -> documentRepository.delete(t));
    }

    @Given("trainer {string} has existing workload of {int} in year {int} month {int}")
    public void trainerHasExistingWorkload(String username, int duration, int year, int month) {
        TrainingMonthSummary monthSummary = new TrainingMonthSummary();
        monthSummary.setMonth(month);
        monthSummary.setTrainingsSummaryDuration((long)duration);

        TrainingYearSummary yearSummary = new TrainingYearSummary();
        yearSummary.setYear(year);
        yearSummary.setMonths(new HashSet<>(List.of(monthSummary)));

        TrainerTrainingSummary summary = new TrainerTrainingSummary();
        summary.setUsername(username);
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setStatus(true);
        summary.setActive(true);
        summary.setYears(new HashSet<>(List.of(yearSummary)));

        trainerSummaryRepository.save(summary);
    }


    @When("an ADD workload event is received for trainer {string} with duration {int} in year {int} month {int}")
    public void addWorkloadEvent(String username, int duration, int year, int month) {
        authenticateAsSystem();
        aggregationService.applyEvent(buildEvent(username, duration, year, month, ActionType.ADD));
    }

    @When("a DELETE workload event is received for trainer {string} with duration {int} in year {int} month {int}")
    public void deleteWorkloadEvent(String username, int duration, int year, int month) {
        authenticateAsSystem();
        aggregationService.applyEvent(buildEvent(username, duration, year, month, ActionType.DELETE));
    }

    @When("client requests workload summary for trainer {string}")
    public void clientRequestsWorkload(String username) throws Exception {
        authenticateAsSystem();
        lastResult = mockMvc.perform(
                get("/trainers/{username}/workload", username)
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    @Then("trainer {string} should have total workload of {int} in year {int} month {int}")
    public void verifyWorkload(String username, int expectedDuration, int year, int month) {
        TrainerTrainingSummary summary = trainerSummaryRepository
                .findByUsername(username)
                .orElseThrow(() -> new AssertionError("No summary found for trainer: " + username));

        long actual = summary.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth().equals(month))
                .mapToLong(TrainingMonthSummary::getTrainingsSummaryDuration)
                .sum();

        assertEquals(expectedDuration, actual,
                "Expected workload " + expectedDuration + " but got " + actual
                        + " for trainer=" + username + " year=" + year + " month=" + month);
    }

    @Then("response status should be {int}")
    public void verifyResponseStatus(int status) throws Exception {
        lastResult.andExpect(status().is(status));
    }

    @Then("response should contain workload of {int} for year {int} month {int}")
    public void verifyResponseBody(int expectedDuration, int year, int month) throws Exception {
        String json = lastResult.andReturn().getResponse().getContentAsString();
        TrainerTrainingSummaryResponseDto dto = objectMapper.readValue(json, TrainerTrainingSummaryResponseDto.class);

        assertNotNull(dto.getYears(), "Years list should not be null");

        long actual = dto.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == month)
                .mapToLong(TrainerTrainingSummaryResponseDto.TrainingMonthSummaryDto::getTrainingsSummaryDuration)
                .sum();

        assertEquals(expectedDuration, actual);
    }

    private TrainerWorkloadRequestDto buildEvent(String username, int duration, int year, int month, ActionType action) {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setTrainerUsername(username);
        dto.setTrainerFirstName("John");
        dto.setTrainerLastName("Doe");
        dto.setIsActive(true);
        dto.setTrainingDate(LocalDate.of(year, month, 1));
        dto.setTrainingDuration(duration);
        dto.setActionType(action);
        return dto;
    }
}
