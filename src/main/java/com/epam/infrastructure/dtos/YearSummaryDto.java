package com.epam.infrastructure.dtos;

import lombok.Data;

import java.util.List;

@Data
public class YearSummaryDto {
    private int year;
    private List<MonthSummaryDto> months;
}
