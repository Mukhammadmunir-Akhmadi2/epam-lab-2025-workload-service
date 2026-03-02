package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerTrainingSummaryDao;
import com.epam.infrastructure.dtos.TrainerTrainingSummaryResponseDto;
import com.epam.model.TrainerTrainingSummary;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Comparator;

@Mapper(componentModel = "spring")
public interface TrainerTrainingSummaryMapper {

    TrainerTrainingSummary toModel(TrainerTrainingSummaryDao dao);
    TrainerTrainingSummaryDao toDao(TrainerTrainingSummary model);

    TrainerTrainingSummaryResponseDto toDto(TrainerTrainingSummary trainerSummary);

    @AfterMapping
    default void sortAfterMapping(
            @MappingTarget TrainerTrainingSummaryResponseDto dto
    ) {
        if (dto.getYears() == null) return;

        dto.getYears().sort(
                Comparator.comparingInt(
                        TrainerTrainingSummaryResponseDto.TrainingYearSummaryDto::getYear
                )
        );

        dto.getYears().forEach(year ->
                year.getMonths().sort(
                        Comparator.comparingInt(
                                TrainerTrainingSummaryResponseDto.TrainingMonthSummaryDto::getMonth
                        )
                )
        );
    }
}