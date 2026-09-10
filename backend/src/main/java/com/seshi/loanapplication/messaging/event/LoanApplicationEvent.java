package com.seshi.loanapplication.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.seshi.loanapplication.dto.CompanyResponse;

/**
 * Safe integration event for downstream loan-processing services.
 *
 * Identity and contact data, including SSN, email, phone number and raw credit
 * score, are intentionally excluded from this Kafka contract.
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

    public static LoanApplicationEvent from(
            CompanyResponse company,
            LoanApplicationEventType eventType) {
        return new LoanApplicationEvent(
                2,
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                company.id(),
                company.companyName(),
                company.industry(),
                company.countryOfRegistration(),
                company.yearsInBusiness(),
                company.annualRevenue(),
                company.totalAssets(),
                CreditRiskBand.fromScore(company.creditScore()),
                company.loanType(),
                company.requestedLoanAmount(),
                company.requestedTermMonths(),
                company.loanPurpose(),
                company.applicationStatus());
    }
}
