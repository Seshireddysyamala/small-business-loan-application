package com.seshi.underwriting.messaging.subscriber;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionEvent;
import com.seshi.underwriting.messaging.publisher.LoanDecisionEventPublisher;
import com.seshi.underwriting.service.UnderwritingDecisionService;

@Component
@ConditionalOnProperty(
        prefix = "underwriting.kafka",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LoanApplicationEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationEventSubscriber.class);

    private final UnderwritingDecisionService decisionService;
    private final LoanDecisionEventPublisher decisionPublisher;

    public LoanApplicationEventSubscriber(
            UnderwritingDecisionService decisionService,
            LoanDecisionEventPublisher decisionPublisher) {
        this.decisionService = decisionService;
        this.decisionPublisher = decisionPublisher;
    }

    @KafkaListener(
            topics = "${underwriting.kafka.input-topic}",
            groupId = "${underwriting.kafka.consumer-group}")
    public void receive(ConsumerRecord<String, LoanApplicationEvent> record) {
        LoanApplicationEvent event = record.value();
        if (event == null) {
            log.info(
                    "Application event tombstone ignored: key={}, topic={}, partition={}, offset={}",
                    record.key(),
                    record.topic(),
                    record.partition(),
                    record.offset());
            return;
        }

        String expectedKey = event.applicationId().toString();
        if (!expectedKey.equals(record.key())) {
            throw new IllegalArgumentException("Application event key does not match application ID");
        }

        log.info(
                "Application event received for underwriting: eventId={}, eventType={}, applicationId={}, topic={}, partition={}, offset={}",
                event.eventId(),
                event.eventType(),
                event.applicationId(),
                record.topic(),
                record.partition(),
                record.offset());

        LoanDecisionEvent decision = decisionService.decide(event);
        decisionPublisher.publish(decision);
    }
}
