package com.epam.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainerMonthlyWorkload {
    private String tmwId;
    @NotNull
    private TrainerSummary trainer;
    @NotNull
    private Integer year;
    @NotNull
    private Integer month;
    @NotNull
    private Integer totalDuration;
}
