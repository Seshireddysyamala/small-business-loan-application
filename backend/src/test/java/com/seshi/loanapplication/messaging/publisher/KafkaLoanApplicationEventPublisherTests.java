package com.seshi.loanapplication.messaging.publisher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.seshi.loanapplication.config.LoanKafkaProperties;
import com.seshi.loanapplication.messaging.event.CreditRiskBand;
import com.seshi.loanapplication.messaging.event.LoanApplicationEvent;
import com.seshi.loanapplication.messaging.event.LoanApplicationEventType;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class KafkaLoanApplicationEventPublisherTests {

    private static final String TOPIC = "loan-application-events";

    @Mock
    private KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate;

    @AfterEach
    void cleanUpTransactionState() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void sendsImmediatelyWhenThereIsNoDatabaseTransaction() {
        LoanApplicationEvent event = event();
        CompletableFuture<SendResult<String, LoanApplicationEvent>> result = new CompletableFuture<>();
        when(kafkaTemplate.send(TOPIC, "42", event)).thenReturn(result);

        publisher().publish(event);

        verify(kafkaTemplate).send(TOPIC, "42", event);
    }

    @Test
    void waitsUntilTheDatabaseTransactionCommitsBeforeSending() {
        LoanApplicationEvent event = event();
        CompletableFuture<SendResult<String, LoanApplicationEvent>> result = new CompletableFuture<>();
        when(kafkaTemplate.send(TOPIC, "42", event)).thenReturn(result);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher().publish(event);

        verifyNoInteractions(kafkaTemplate);
        for (TransactionSynchronization synchronization
                : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(kafkaTemplate).send(TOPIC, "42", event);
    }

    private KafkaLoanApplicationEventPublisher publisher() {
        return new KafkaLoanApplicationEventPublisher(
                kafkaTemplate,
                new LoanKafkaProperties(TOPIC, "loan-decision-events", 3, (short) 1));
    }

    private LoanApplicationEvent event() {
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
