package com.epam.infrastructure.daos;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TrainingYearSummaryDao {
    private Integer year;
    private Set<TrainingMonthSummaryDao> months = new HashSet<>();
}
