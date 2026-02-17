package com.epam.infrastructure.messaging;

import com.epam.application.services.impl.WorkloadAggregationServiceImpl;
import com.epam.infrastructure.dtos.TrainerWorkloadRequestDto;
import com.epam.infrastructure.logging.TransactionIdFilter;
import com.epam.infrastructure.security.JwtAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkloadEventsConsumerTest {

    private final JwtAuthenticator authenticator = mock(JwtAuthenticator.class);
    private final WorkloadAggregationServiceImpl service = mock(WorkloadAggregationServiceImpl.class);

    private final WorkloadEventsConsumer consumer = new WorkloadEventsConsumer(authenticator, service);

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void onMessage_shouldAuthenticate_applyEvent_andClearMdcAndSecurityContext_whenTxIdPresent() {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setTrainerUsername("john");
        dto.setTrainingDate(LocalDate.of(2026, 2, 17));

        String txId = "tx-123";
        String auth = "Bearer token";

        // Pre-set context to ensure it gets cleared
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", "p")
        );
        MDC.put(TransactionIdFilter.TRANSACTION_ID_HEADER, "old");

        consumer.onMessage(dto, txId, auth);

        verify(authenticator, times(1)).authenticate(auth);
        verify(service, times(1)).applyEvent(dto);

        // Must be cleared in finally
        assertNull(MDC.get(TransactionIdFilter.TRANSACTION_ID_HEADER));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void onMessage_shouldNotSetMdc_whenTxIdIsNull_butStillClearAtEnd() {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setTrainerUsername("john");
        dto.setTrainingDate(LocalDate.of(2026, 2, 17));

        String auth = "Bearer token";

        // Ensure it starts empty
        MDC.remove(TransactionIdFilter.TRANSACTION_ID_HEADER);

        consumer.onMessage(dto, null, auth);

        verify(authenticator, times(1)).authenticate(auth);
        verify(service, times(1)).applyEvent(dto);

        // Still cleared (no leftover)
        assertNull(MDC.get(TransactionIdFilter.TRANSACTION_ID_HEADER));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void onMessage_shouldClearMdcAndSecurityContext_evenIfServiceThrows() {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setTrainerUsername("john");
        dto.setTrainingDate(LocalDate.of(2026, 2, 17));

        String txId = "tx-999";
        String auth = "Bearer token";

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", "p")
        );

        doThrow(new RuntimeException("boom")).when(service).applyEvent(dto);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> consumer.onMessage(dto, txId, auth));

        assertTrue(ex.getMessage().contains("boom"));

        verify(authenticator, times(1)).authenticate(auth);
        verify(service, times(1)).applyEvent(dto);

        // Must be cleared in finally
        assertNull(MDC.get(TransactionIdFilter.TRANSACTION_ID_HEADER));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void onMessage_shouldClearSecurityContext_evenIfAuthenticatorThrows() {
        TrainerWorkloadRequestDto dto = new TrainerWorkloadRequestDto();
        dto.setTrainerUsername("john");
        dto.setTrainingDate(LocalDate.of(2026, 2, 17));

        String txId = "tx-777";
        String auth = "Bearer bad";

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", "p")
        );

        doThrow(new RuntimeException("auth failed")).when(authenticator).authenticate(auth);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> consumer.onMessage(dto, txId, auth));

        assertTrue(ex.getMessage().contains("auth failed"));

        verify(authenticator, times(1)).authenticate(auth);
        verifyNoInteractions(service);

        // Important: txId is set AFTER authenticate in your code, so MDC should remain empty
        assertNull(MDC.get(TransactionIdFilter.TRANSACTION_ID_HEADER));
        // Security must be cleared in finally
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
