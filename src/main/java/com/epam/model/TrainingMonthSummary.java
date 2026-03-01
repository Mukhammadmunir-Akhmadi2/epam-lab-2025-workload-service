package com.epam.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainingMonthSummary {
    @NotNull
    private Integer month;

    @NotNull
    private Long trainingsSummaryDuration;
}