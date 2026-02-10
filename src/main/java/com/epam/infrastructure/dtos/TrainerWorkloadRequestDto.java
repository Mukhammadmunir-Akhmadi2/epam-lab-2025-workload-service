package com.epam.infrastructure.dtos;

import com.epam.infrastructure.enums.ActionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainerWorkloadRequestDto {
    @NotBlank
    private String trainerUsername;
    @NotBlank
    private String trainerFirstName;
    @NotBlank
    private String trainerLastName;
    @NotNull
    private Boolean isActive;
    @NotNull
    private LocalDate trainingDate;
    @Min(1)
    @NotNull
    private Integer trainingDuration;

    @NotNull
    private ActionType actionType;
}