package com.epam.infrastructure.dtos;

import lombok.Data;

import java.util.List;

@Data
public class TrainerMonthlySummaryResponseDto {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerStatus;
    private List<YearSummaryDto> years;
}
