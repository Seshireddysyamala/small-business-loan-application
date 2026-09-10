package com.seshi.underwriting.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The underwriting service owns its representation of the public application
 * event contract instead of depending on backend implementation classes.
 */
public record LoanApplicationEvent(
        int schemaVersion,
        UUID eventId,
        LoanApplicationEventType eventType,
        Instant occurredAt,
        Long applicationId,
        String companyName,
        String industry,
        String countryOfRegistration,
        Integer yearsInBusiness,
        BigDecimal annualRevenue,
        BigDecimal totalAssets,
        CreditRiskBand creditRiskBand,
        String loanType,
        BigDecimal requestedLoanAmount,
        Integer requestedTermMonths,
        String loanPurpose,
        String applicationStatus) {
}
