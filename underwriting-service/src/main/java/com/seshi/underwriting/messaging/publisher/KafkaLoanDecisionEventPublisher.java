package com.seshi.underwriting.messaging.publisher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.seshi.underwriting.config.UnderwritingKafkaProperties;
import com.seshi.underwriting.messaging.event.LoanDecisionEvent;

@Component
@ConditionalOnProperty(
        prefix = "underwriting.kafka",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class KafkaLoanDecisionEventPublisher implements LoanDecisionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaLoanDecisionEventPublisher.class);

    private final KafkaTemplate<String, LoanDecisionEvent> kafkaTemplate;
    private final UnderwritingKafkaProperties properties;

    public KafkaLoanDecisionEventPublisher(
            KafkaTemplate<String, LoanDecisionEvent> kafkaTemplate,
            UnderwritingKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(LoanDecisionEvent event) {
        try {
            SendResult<String, LoanDecisionEvent> result = kafkaTemplate
                    .send(properties.outputTopic(), event.applicationId().toString(), event)
                    .get(10, TimeUnit.SECONDS);

            log.info(
                    "Loan decision published: decisionId={}, applicationId={}, decision={}, topic={}, partition={}, offset={}",
                    event.decisionId(),
                    event.applicationId(),
                    event.decision(),
                    properties.outputTopic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new DecisionPublishingException("Interrupted while publishing loan decision", failure);
        } catch (ExecutionException | TimeoutException failure) {
            throw new DecisionPublishingException("Could not publish loan decision", failure);
        }
    }
}
