package com.epam.infrastructure.dtos;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(
        name = "TrainerTrainingSummaryResponse",
        description = "Represents trainer training summary grouped by year and month"
)
public class TrainerTrainingSummaryResponseDto {

    @Schema(description = "Unique username of the trainer", example = "John.Doe")
    private String username;

    @Schema(description = "Trainer first name", example = "John")
    private String firstName;

    @Schema(description = "Trainer last name", example = "Doe")
    private String lastName;

    @Schema(description = "Trainer status (true - active, false - inactive)", example = "true")
    private Boolean status;

    @ArraySchema(
            schema = @Schema(
                    description = "List of yearly training summaries"
            )
    )
    private List<TrainingYearSummaryDto> years;


    @Data
    @Schema(name = "TrainingYearSummary", description = "Represents yearly summary of trainer trainings")
    public static class TrainingYearSummaryDto {

        @Schema(description = "Year of trainings", example = "2026")
        private int year;

        @ArraySchema(
                schema = @Schema(
                        description = "List of monthly training summaries"
                )
        )
        private List<TrainingMonthSummaryDto> months;
    }


    @Data
    @Schema(name = "TrainingMonthSummary", description = "Represents monthly training summary")
    public static class TrainingMonthSummaryDto {

        @Schema(description = "Month number (1-12)", example = "2")
        private int month;

        @Schema(description = "Total training duration in minutes for the month", example = "120")
        private long trainingsSummaryDuration;
    }
}