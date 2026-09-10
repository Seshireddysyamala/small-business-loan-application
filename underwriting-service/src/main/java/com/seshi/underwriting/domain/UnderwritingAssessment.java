package com.seshi.underwriting.domain;

import java.math.BigDecimal;
import java.util.List;

import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;

public record UnderwritingAssessment(
        LoanDecisionOutcome decision,
        BigDecimal approvedAmount,
        List<String> reasonCodes) {

    public UnderwritingAssessment {
        reasonCodes = List.copyOf(reasonCodes);
    }
}
