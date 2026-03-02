package com.epam.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TrainerTrainingSummary {
    private String trainerId;
    @NotBlank
    private String username;
    private String firstName;
    private String lastName;
    @NotNull
    private Boolean status;
    @NotNull
    private Boolean active;

    @Valid
    @NotNull
    private Set<TrainingYearSummary> years = new HashSet<>();
}
