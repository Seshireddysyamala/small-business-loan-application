package com.seshi.loanapplication.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "loan.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoanKafkaProperties.class)
public class KafkaConfiguration {

    @Bean
    NewTopic loanApplicationEventsTopic(LoanKafkaProperties properties) {
        return TopicBuilder.name(properties.topic())
                .partitions(properties.partitions())
                .replicas(properties.replicas())
                .build();
    }

    @Bean
    NewTopic loanDecisionEventsTopic(LoanKafkaProperties properties) {
        return TopicBuilder.name(properties.decisionTopic())
                .partitions(properties.partitions())
                .replicas(properties.replicas())
                .build();
    }
}
