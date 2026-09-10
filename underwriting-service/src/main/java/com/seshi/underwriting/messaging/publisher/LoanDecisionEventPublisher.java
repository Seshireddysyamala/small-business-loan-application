package com.seshi.underwriting.messaging.publisher;

import com.seshi.underwriting.messaging.event.LoanDecisionEvent;

public interface LoanDecisionEventPublisher {

    void publish(LoanDecisionEvent event);
}
