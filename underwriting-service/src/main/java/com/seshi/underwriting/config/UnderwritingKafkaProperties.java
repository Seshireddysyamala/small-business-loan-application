package com.seshi.underwriting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("underwriting.kafka")
public record UnderwritingKafkaProperties(
        String inputTopic,
        String outputTopic,
        String consumerGroup,
        int partitions,
        short replicas) {
}
