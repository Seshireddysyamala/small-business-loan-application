package com.seshi.loanapplication.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoanDecisionEvent(
        int schemaVersion,
        UUID decisionId,
        UUID sourceEventId,
        Long applicationId,
        LoanDecisionOutcome decision,
        BigDecimal approvedAmount,
        List<String> reasonCodes,
        String policyVersion,
        Instant decidedAt) {
}
