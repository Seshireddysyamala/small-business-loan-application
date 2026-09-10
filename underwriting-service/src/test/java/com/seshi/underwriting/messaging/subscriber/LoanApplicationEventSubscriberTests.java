package com.seshi.underwriting.messaging.subscriber;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seshi.underwriting.messaging.event.CreditRiskBand;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanApplicationEventType;
import com.seshi.underwriting.messaging.event.LoanDecisionEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;
import com.seshi.underwriting.messaging.publisher.LoanDecisionEventPublisher;
import com.seshi.underwriting.service.UnderwritingDecisionService;

@ExtendWith(MockitoExtension.class)
class LoanApplicationEventSubscriberTests {

    @Mock
    private UnderwritingDecisionService decisionService;

    @Mock
    private LoanDecisionEventPublisher decisionPublisher;

    @Test
    void consumesAnApplicationEventAndPublishesItsDecision() {
        LoanApplicationEvent applicationEvent = applicationEvent();
        LoanDecisionEvent decisionEvent = new LoanDecisionEvent(
                1,
                UUID.randomUUID(),
                applicationEvent.eventId(),
                42L,
                LoanDecisionOutcome.APPROVED,
                new BigDecimal("250000.00"),
                List.of("DEMO_POLICY_REQUIREMENTS_MET"),
                "demo-policy-v1",
                Instant.now());
        when(decisionService.decide(applicationEvent)).thenReturn(decisionEvent);

        new LoanApplicationEventSubscriber(decisionService, decisionPublisher).receive(
                new ConsumerRecord<>("loan-application-events", 0, 10L, "42", applicationEvent));

        verify(decisionService).decide(applicationEvent);
        verify(decisionPublisher).publish(decisionEvent);
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
