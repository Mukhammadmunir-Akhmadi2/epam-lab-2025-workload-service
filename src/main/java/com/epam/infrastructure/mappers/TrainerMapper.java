package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerSummaryDao;
import com.epam.model.TrainerSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommonMapper.class)
public interface TrainerMapper {
    @Mapping(source = "trainerId", target = "trainerId", qualifiedByName = "uuidToString")
    TrainerSummary toModel(TrainerSummaryDao dao);
    @Mapping(source = "trainerId", target = "trainerId", qualifiedByName = "stringToUuid")
    TrainerSummaryDao toDao(TrainerSummary model);
}