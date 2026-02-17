package com.epam.infrastructure.messaging;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.logging.TransactionIdFilter;
import com.epam.infrastructure.security.JwtAuthenticator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Log4j2
@Validated
@Component
@RequiredArgsConstructor
public class WorkloadEventsConsumer {

    private final JwtAuthenticator authenticator;
    private final WorkloadAggregationServiceImpl service;

    @KafkaListener(
            topics = "${app.kafka.topics.workload-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(
            @Valid TrainerWorkloadRequestDto event,
            @Header(name = TransactionIdFilter.TRANSACTION_ID_HEADER, required = false) String txId,
            @Header(name = "Authorization") String authorization
    ) {
        try {
            authenticator.authenticate(authorization);
            if (txId != null) MDC.put(TransactionIdFilter.TRANSACTION_ID_HEADER, txId);

            log.info("Workload event consumed. trainer={} date={} action={}",
                    event.getTrainerUsername(), event.getTrainingDate(), event.getActionType());

            service.applyEvent(event);
        } finally {
            MDC.remove(TransactionIdFilter.TRANSACTION_ID_HEADER);
            SecurityContextHolder.clearContext();
        }
    }
}