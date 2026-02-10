package com.epam.infrastructure.mappers;

import com.epam.infrastructure.daos.TrainerMonthlyWorkloadDao;
import com.epam.model.TrainerMonthlyWorkload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TrainerMapper.class, CommonMapper.class})
public interface TrainerMonthlyWorkloadMapper {

    @Mapping(source = "tmwId", target = "tmwId", qualifiedByName = "uuidToString")
    TrainerMonthlyWorkload toModel(TrainerMonthlyWorkloadDao dao);

    @Mapping(source = "tmwId", target = "tmwId", qualifiedByName = "stringToUuid")
    TrainerMonthlyWorkloadDao toDao(TrainerMonthlyWorkload model);
}