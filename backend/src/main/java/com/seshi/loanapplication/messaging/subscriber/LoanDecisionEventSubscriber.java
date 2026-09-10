package com.seshi.loanapplication.messaging.subscriber;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.seshi.loanapplication.messaging.event.LoanDecisionEvent;

@Component
@ConditionalOnProperty(
        prefix = "loan.kafka",
        name = { "enabled", "decision-consumer-enabled" },
        havingValue = "true",
        matchIfMissing = true)
public class LoanDecisionEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(LoanDecisionEventSubscriber.class);

    private final LoanDecisionEventHandler eventHandler;

    public LoanDecisionEventSubscriber(LoanDecisionEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @KafkaListener(
            topics = "${loan.kafka.decision-topic}",
            groupId = "${loan.kafka.decision-consumer-group}")
    public void receive(ConsumerRecord<String, LoanDecisionEvent> record) {
        LoanDecisionEvent event = record.value();
        if (event == null) {
            log.info(
                    "Loan decision tombstone ignored: key={}, topic={}, partition={}, offset={}",
                    record.key(),
                    record.topic(),
                    record.partition(),
                    record.offset());
            return;
        }

        String expectedKey = event.applicationId().toString();
        if (!expectedKey.equals(record.key())) {
            throw new IllegalArgumentException("Loan decision key does not match application ID");
        }

        log.info(
                "Loan decision received: decisionId={}, applicationId={}, decision={}, topic={}, partition={}, offset={}",
                event.decisionId(),
                event.applicationId(),
                event.decision(),
                record.topic(),
                record.partition(),
                record.offset());
        eventHandler.apply(event);
    }
}
