package com.seshi.underwriting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seshi.underwriting.domain.UnderwritingAssessment;
import com.seshi.underwriting.domain.UnderwritingPolicy;
import com.seshi.underwriting.entity.UnderwritingDecision;
import com.seshi.underwriting.messaging.event.CreditRiskBand;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanApplicationEventType;
import com.seshi.underwriting.messaging.event.LoanDecisionEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;
import com.seshi.underwriting.repository.UnderwritingDecisionRepository;

@ExtendWith(MockitoExtension.class)
class UnderwritingDecisionServiceTests {

    @Mock
    private UnderwritingDecisionRepository repository;

    @Mock
    private UnderwritingPolicy policy;

    @Test
    void reusesThePersistedDecisionWhenKafkaRedeliversTheSameEvent() {
        LoanApplicationEvent sourceEvent = applicationEvent();
        UnderwritingAssessment assessment = new UnderwritingAssessment(
                LoanDecisionOutcome.APPROVED,
                sourceEvent.requestedLoanAmount(),
                List.of("DEMO_POLICY_REQUIREMENTS_MET"));
        Instant decidedAt = Instant.parse("2026-09-05T12:00:00Z");
        UnderwritingDecision stored = UnderwritingDecision.create(
                sourceEvent,
                assessment,
                UnderwritingPolicy.VERSION,
                decidedAt);
        when(repository.findBySourceEventId(sourceEvent.eventId().toString()))
                .thenReturn(Optional.empty());
        when(policy.assess(sourceEvent)).thenReturn(assessment);
        when(repository.save(any(UnderwritingDecision.class))).thenReturn(stored);
        UnderwritingDecisionService service = new UnderwritingDecisionService(
                repository,
                policy,
                Clock.fixed(decidedAt, ZoneOffset.UTC));

        LoanDecisionEvent first = service.decide(sourceEvent);
        when(repository.findBySourceEventId(sourceEvent.eventId().toString()))
                .thenReturn(Optional.of(stored));
        LoanDecisionEvent redelivered = service.decide(sourceEvent);

        assertEquals(first.decisionId(), redelivered.decisionId());
        assertEquals(sourceEvent.eventId(), redelivered.sourceEventId());
        verify(repository, times(1)).save(any(UnderwritingDecision.class));
        verify(policy, times(1)).assess(sourceEvent);
    }

    private LoanApplicationEvent applicationEvent() {
        return new LoanApplicationEvent(
                2,
                UUID.randomUUID(),
                LoanApplicationEventType.CREATED,
                Instant.now(),
                42L,
                "Acme Manufacturing LLC",
                "Manufacturing",
                "US",
                8,
                new BigDecimal("1800000.00"),
                new BigDecimal("950000.00"),
                CreditRiskBand.MEDIUM,
                "TERM_LOAN",
                new BigDecimal("250000.00"),
                60,
                "Equipment purchase",
                "SUBMITTED");
    }
}
