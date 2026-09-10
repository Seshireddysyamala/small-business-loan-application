package com.seshi.loanapplication.messaging.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.seshi.loanapplication.config.LoanKafkaProperties;
import com.seshi.loanapplication.messaging.event.LoanApplicationEvent;

@Component
@ConditionalOnProperty(prefix = "loan.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class  KafkaLoanApplicationEventPublisher implements LoanApplicationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaLoanApplicationEventPublisher.class);

    private final KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate;
    private final LoanKafkaProperties properties;

    public KafkaLoanApplicationEventPublisher(
            KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate,
            LoanKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(LoanApplicationEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
            return;
        }

        send(event);
    }

    private void send(LoanApplicationEvent event) {
        String applicationKey = event.applicationId().toString();

        try {
            kafkaTemplate.send(properties.topic(), applicationKey, event)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            log.error(
                                    "Kafka event delivery failed: eventId={}, applicationId={}, topic={}",
                                    event.eventId(),
                                    event.applicationId(),
                                    properties.topic(),
                                    failure);
                            return;
                        }

                        log.info(
                                "Kafka event delivered: eventId={}, applicationId={}, topic={}, partition={}, offset={}",
                                event.eventId(),
                                event.applicationId(),
                                properties.topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });
        } catch (RuntimeException failure) {
            log.error(
                    "Kafka event send could not start: eventId={}, applicationId={}, topic={}",
                    event.eventId(),
                    event.applicationId(),
                    properties.topic(),
                    failure);
        }
    }
}
