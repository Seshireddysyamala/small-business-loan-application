package com.seshi.loanapplication.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String companyName,
        String businessStructure,
        String industry,
        String countryOfRegistration,
        Integer yearsInBusiness,
        BigDecimal annualRevenue,
        BigDecimal totalAssets,
        String loanType,
        BigDecimal requestedLoanAmount,
        Integer requestedTermMonths,
        String loanPurpose,
        Integer creditScore,
        String contactName,
        String ssnLastFour,
        String contactEmail,
        String contactPhone,
        String applicationStatus,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt) {
}
