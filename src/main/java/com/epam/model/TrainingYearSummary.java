package com.epam.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TrainingYearSummary {
    @NotNull
    private Integer year;

    @Valid
    @NotNull
    private Set<TrainingMonthSummary> months = new HashSet<>();

}
