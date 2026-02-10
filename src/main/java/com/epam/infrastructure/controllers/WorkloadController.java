package com.epam.infrastructure.controllers;

import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/trainers")
@Tag(
        name = "Trainer Workload",
        description = "Endpoints for retrieving trainer monthly workload summaries."
)
public interface WorkloadController {

    @GetMapping("/{username}/workload")
    @Operation(
            summary = "Get trainer workload summary",
            description =
                    "Returns the trainer's monthly workload aggregated by year and month. " +
                            "The response is built from workload events (ADD/DELETE) stored in the workload microservice database.",
            security = @SecurityRequirement(name = "BearerAuth"),
            parameters = {
                    @Parameter(
                            name = "username",
                            description = "Trainer username to retrieve workload summary for",
                            required = true,
                            example = "john.smith"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Workload summary returned successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)"),
                    @ApiResponse(responseCode = "403", description = "Forbidden (token lacks required scope/authority)"),
                    @ApiResponse(responseCode = "404", description = "Trainer not found")
            }
    )
    ResponseEntity<TrainerMonthlySummaryResponseDto> getTrainerWorkloadSummary(
            @PathVariable String username
    );
}
