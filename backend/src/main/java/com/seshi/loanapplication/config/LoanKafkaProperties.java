package com.seshi.loanapplication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loan.kafka")
public record LoanKafkaProperties(
        String topic,
        String decisionTopic,
        int partitions,
        short replicas) {
}
