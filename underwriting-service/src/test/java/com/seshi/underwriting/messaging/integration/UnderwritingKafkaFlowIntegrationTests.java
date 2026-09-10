package com.seshi.underwriting.messaging.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.seshi.underwriting.messaging.event.CreditRiskBand;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanApplicationEventType;
import com.seshi.underwriting.repository.UnderwritingDecisionRepository;

@SpringBootTest(properties = {
        "underwriting.kafka.enabled=true",
        "underwriting.kafka.input-topic=loan-application-events-test",
        "underwriting.kafka.output-topic=loan-decision-events-test",
        "underwriting.kafka.consumer-group=loan-underwriting-service-test",
        "underwriting.kafka.partitions=1",
        "underwriting.kafka.replicas=1"
})
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                UnderwritingKafkaFlowIntegrationTests.INPUT_TOPIC,
                UnderwritingKafkaFlowIntegrationTests.OUTPUT_TOPIC
        })
@DirtiesContext
class UnderwritingKafkaFlowIntegrationTests {

    static final String INPUT_TOPIC = "loan-application-events-test";
    static final String OUTPUT_TOPIC = "loan-decision-events-test";

    @Autowired
    private KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private UnderwritingDecisionRepository decisionRepository;

    private Consumer<String, String> decisionConsumer;

    @BeforeEach
    void createDecisionConsumer() {
        Map<String, Object> properties = KafkaTestUtils.consumerProps(
                embeddedKafka,
                "underwriting-decision-verifier",
                false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        decisionConsumer = new DefaultKafkaConsumerFactory<String, String>(properties).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(decisionConsumer, OUTPUT_TOPIC);
    }

    @AfterEach
    void closeDecisionConsumer() {
        decisionConsumer.close();
    }

    @Test
    void consumesApplicationPersistsDecisionAndPublishesIdempotentResult() throws Exception {
        LoanApplicationEvent applicationEvent = applicationEvent();

        kafkaTemplate.send(INPUT_TOPIC, "42", applicationEvent).get(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> firstDecision = KafkaTestUtils.getSingleRecord(
                decisionConsumer,
                OUTPUT_TOPIC,
                Duration.ofSeconds(10));

        assertEquals("42", firstDecision.key());
        assertTrue(firstDecision.value().contains("\"decision\":\"APPROVED\""));
        assertTrue(firstDecision.value().contains("\"sourceEventId\":\"" + applicationEvent.eventId() + "\""));
        assertTrue(firstDecision.value().contains("\"approvedAmount\":250000.00"));
        assertEquals(1, decisionRepository.count());

        kafkaTemplate.send(INPUT_TOPIC, "42", applicationEvent).get(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> repeatedDecision = KafkaTestUtils.getSingleRecord(
                decisionConsumer,
                OUTPUT_TOPIC,
                Duration.ofSeconds(10));

        assertEquals(
                jsonStringField(firstDecision.value(), "decisionId"),
                jsonStringField(repeatedDecision.value(), "decisionId"));
        assertTrue(repeatedDecision.value().contains("\"decision\":\"APPROVED\""));
        assertEquals(1, decisionRepository.count());
    }

    private String jsonStringField(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\\\"" + fieldName + "\\\":\\\"([^\\\"]+)\\\"")
                .matcher(json);
        assertTrue(matcher.find(), "Expected JSON field " + fieldName);
        return matcher.group(1);
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
