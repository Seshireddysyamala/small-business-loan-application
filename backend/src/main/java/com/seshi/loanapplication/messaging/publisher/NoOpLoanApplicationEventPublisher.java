package com.seshi.loanapplication.messaging.publisher;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.seshi.loanapplication.messaging.event.LoanApplicationEvent;

@Component
@ConditionalOnProperty(prefix = "loan.kafka", name = "enabled", havingValue = "false")
public class NoOpLoanApplicationEventPublisher implements LoanApplicationEventPublisher {

    @Override
    public void publish(LoanApplicationEvent event) {
        // Kafka is intentionally disabled for this environment.
    }
}
