package com.seshi.underwriting.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.seshi.underwriting.messaging.event.CreditRiskBand;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;

/**
 * Demonstration policy only. It is deliberately small, explainable and based
 * only on business financial fields; it is not a production lending model.
 */
@Component
public class UnderwritingPolicy {

    public static final String VERSION = "demo-policy-v1";

    private static final BigDecimal REVENUE_FACTOR = new BigDecimal("0.25");
    private static final BigDecimal ASSET_FACTOR = new BigDecimal("0.10");

    public UnderwritingAssessment assess(LoanApplicationEvent event) {
        if (event.schemaVersion() != 2) {
            return manualReview("UNSUPPORTED_APPLICATION_EVENT_VERSION");
        }

        if (event.yearsInBusiness() == null
                || event.annualRevenue() == null
                || event.totalAssets() == null
                || event.requestedLoanAmount() == null
                || event.creditRiskBand() == null) {
            return manualReview("INCOMPLETE_FINANCIAL_DATA");
        }

        if (event.annualRevenue().signum() <= 0) {
            return new UnderwritingAssessment(
                    LoanDecisionOutcome.DECLINED,
                    null,
                    List.of("NO_REPORTED_REVENUE"));
        }

        if (event.yearsInBusiness() < 2) {
            return manualReview("LIMITED_OPERATING_HISTORY");
        }

        if (event.creditRiskBand() == CreditRiskBand.HIGH) {
            return manualReview("HIGH_CREDIT_RISK_BAND");
        }

        BigDecimal supportedAmount = event.annualRevenue()
                .multiply(REVENUE_FACTOR)
                .add(event.totalAssets().multiply(ASSET_FACTOR))
                .setScale(2, RoundingMode.HALF_UP);

        if (event.requestedLoanAmount().compareTo(supportedAmount) > 0) {
            return manualReview("REQUEST_EXCEEDS_DEMO_CAPACITY");
        }

        return new UnderwritingAssessment(
                LoanDecisionOutcome.APPROVED,
                event.requestedLoanAmount(),
                List.of("DEMO_POLICY_REQUIREMENTS_MET"));
    }

    private UnderwritingAssessment manualReview(String reasonCode) {
        return new UnderwritingAssessment(
                LoanDecisionOutcome.MANUAL_REVIEW,
                null,
                List.of(reasonCode));
    }
}
