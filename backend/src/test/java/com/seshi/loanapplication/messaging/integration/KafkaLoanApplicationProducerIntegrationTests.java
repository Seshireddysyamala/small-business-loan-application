package com.seshi.loanapplication.messaging.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;
import com.seshi.loanapplication.messaging.event.LoanDecisionEvent;
import com.seshi.loanapplication.messaging.event.LoanDecisionOutcome;
import com.seshi.loanapplication.service.CompanyService;

@SpringBootTest(properties = {
        "loan.kafka.enabled=true",
        "loan.kafka.topic=loan-application-events-test",
        "loan.kafka.decision-topic=loan-decision-events-test",
        "loan.kafka.decision-consumer-group=loan-decision-updater-test",
        "loan.kafka.partitions=1",
        "loan.kafka.replicas=1"
})
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                KafkaLoanApplicationProducerIntegrationTests.APPLICATION_TOPIC,
                KafkaLoanApplicationProducerIntegrationTests.DECISION_TOPIC
        })
@DirtiesContext
class KafkaLoanApplicationProducerIntegrationTests {

    static final String APPLICATION_TOPIC = "loan-application-events-test";
    static final String DECISION_TOPIC = "loan-decision-events-test";

    @Autowired
    private CompanyService companyService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<String, LoanDecisionEvent> kafkaTemplate;

    private Consumer<String, String> consumer;

    @BeforeEach
    void createConsumer() {
        Map<String, Object> properties = KafkaTestUtils.consumerProps(
                embeddedKafka,
                "loan-application-producer-integration",
                false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new DefaultKafkaConsumerFactory<String, String>(properties).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, APPLICATION_TOPIC);
    }

    @AfterEach
    void closeConsumer() {
        consumer.close();
    }

    @Test
    void applicationIsPublishedSafelyAndReturnedDecisionUpdatesItsStatus() throws Exception {
        CompanyResponse response = companyService.createCompany(validRequest());

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                consumer,
                APPLICATION_TOPIC,
                Duration.ofSeconds(10));

        assertEquals(response.id().toString(), record.key());
        assertTrue(record.value().contains("\"eventType\":\"CREATED\""));
        assertTrue(record.value().contains("\"schemaVersion\":2"));
        assertTrue(record.value().contains("\"applicationId\":" + response.id()));
        assertTrue(record.value().contains("\"requestedLoanAmount\":250000.00"));
        assertTrue(record.value().contains("\"annualRevenue\":1800000.00"));
        assertTrue(record.value().contains("\"creditRiskBand\":\"MEDIUM\""));

        String lowerCasePayload = record.value().toLowerCase();
        assertFalse(lowerCasePayload.contains("ssn"));
        assertFalse(lowerCasePayload.contains("contactemail"));
        assertFalse(lowerCasePayload.contains("contactphone"));
        assertFalse(lowerCasePayload.contains("creditscore"));

        LoanDecisionEvent decision = new LoanDecisionEvent(
                1,
                UUID.randomUUID(),
                UUID.randomUUID(),
                response.id(),
                LoanDecisionOutcome.APPROVED,
                new BigDecimal("250000.00"),
                List.of("DEMO_POLICY_REQUIREMENTS_MET"),
                "demo-v1",
                Instant.now());
        kafkaTemplate.send(DECISION_TOPIC, response.id().toString(), decision)
                .get(10, TimeUnit.SECONDS);

        assertEquals("APPROVED", awaitApplicationStatus(response.id()));
    }

    private String awaitApplicationStatus(Long applicationId) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        String status;
        do {
            status = companyService.getCompanyById(applicationId).applicationStatus();
            if ("APPROVED".equals(status)) {
                return status;
            }
            Thread.sleep(50);
        } while (System.nanoTime() < deadline);
        return status;
    }

    private CompanyRequest validRequest() {
        return new CompanyRequest(
                "Acme Manufacturing LLC",
                "LLC",
                "Manufacturing",
                "US",
                8,
                new BigDecimal("1800000.00"),
                new BigDecimal("950000.00"),
                "TERM_LOAN",
                new BigDecimal("250000.00"),
                60,
                "Equipment purchase",
                735,
                "Jordan Lee",
                "123-45-6789",
                "jordan.lee@example.com",
                "212-555-0147");
    }
}
