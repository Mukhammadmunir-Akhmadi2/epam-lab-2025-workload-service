package com.epam.infrastructure.daos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TrainingMonthSummaryDao {
    @NotNull
    private Integer month;

    @NotNull
    private Long trainingsSummaryDuration;
}
