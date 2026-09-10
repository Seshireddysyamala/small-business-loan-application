package com.seshi.loanapplication.messaging.publisher;

import com.seshi.loanapplication.messaging.event.LoanApplicationEvent;

public interface LoanApplicationEventPublisher {

    void publish(LoanApplicationEvent event);
}
