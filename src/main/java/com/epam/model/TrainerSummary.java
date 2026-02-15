package com.epam.model;

import com.epam.infrastructure.enums.TrainerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainerSummary {
    private String trainerId;
    @NotBlank
    private String username;
    private String firstName;
    private String lastName;
    @NotNull
    private TrainerStatus status;
    @NotNull
    private Boolean active;
}
