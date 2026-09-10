package com.seshi.underwriting.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "underwriting.kafka",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(UnderwritingKafkaProperties.class)
public class UnderwritingKafkaConfiguration {

    @Bean
    NewTopic loanApplicationEventsTopic(UnderwritingKafkaProperties properties) {
        return topic(properties.inputTopic(), properties);
    }

    @Bean
    NewTopic loanDecisionEventsTopic(UnderwritingKafkaProperties properties) {
        return topic(properties.outputTopic(), properties);
    }

    private NewTopic topic(String name, UnderwritingKafkaProperties properties) {
        return TopicBuilder.name(name)
                .partitions(properties.partitions())
                .replicas(properties.replicas())
                .build();
    }
}
