package com.epam.application.services.impl;

import com.epam.application.exceptions.ResourceNotFoundException;
import com.epam.application.repository.TrainerMonthlyWorkloadRepository;
import com.epam.application.repository.TrainerRepository;
import com.epam.application.services.WorkloadQueryService;
import com.epam.infrastructure.dtos.MonthSummaryDto;
import com.epam.infrastructure.dtos.TrainerMonthlySummaryResponseDto;
import com.epam.infrastructure.dtos.YearSummaryDto;
import com.epam.model.TrainerMonthlyWorkload;
import com.epam.model.TrainerSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class WorkloadQueryServiceImpl implements WorkloadQueryService {

    private final TrainerRepository trainerRepository;
    private final TrainerMonthlyWorkloadRepository monthlyRepository;


    @Override
    public TrainerMonthlySummaryResponseDto getSummary(String username) {
        TrainerSummary trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        List<TrainerMonthlyWorkload> rows = monthlyRepository.findAllByTrainer(trainer);

        Map<Integer, List<TrainerMonthlyWorkload>> byYear = rows.stream()
                .collect(Collectors.groupingBy(TrainerMonthlyWorkload::getYear));

        TrainerMonthlySummaryResponseDto resp = new TrainerMonthlySummaryResponseDto();
        resp.setTrainerUsername(trainer.getUsername());
        resp.setTrainerFirstName(trainer.getFirstName());
        resp.setTrainerLastName(trainer.getLastName());
        resp.setTrainerStatus(trainer.getActive());

        List<YearSummaryDto> years = byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    YearSummaryDto y = new YearSummaryDto();
                    y.setYear(e.getKey());
                    List<MonthSummaryDto> months = e.getValue().stream()
                            .sorted(Comparator.comparingInt(TrainerMonthlyWorkload::getMonth))
                            .map(m -> {
                                MonthSummaryDto ms = new MonthSummaryDto();
                                ms.setMonth(m.getMonth());
                                ms.setTrainingSummaryDuration(m.getTotalDuration());
                                return ms;
                            })
                            .toList();
                    y.setMonths(months);
                    return y;
                }).toList();

        resp.setYears(years);

        log.info("Workload summary returned. trainer={}, years={}, rows={}", username, years.size(), rows.size());

        return resp;
    }
}
