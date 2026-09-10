package com.seshi.underwriting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.seshi.underwriting.messaging.event.CreditRiskBand;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanApplicationEventType;
import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;

class UnderwritingPolicyTests {

    private final UnderwritingPolicy policy = new UnderwritingPolicy();

    @Test
    void approvesRequestWithinTheDemonstrationCapacity() {
        UnderwritingAssessment assessment = policy.assess(event(
                2,
                8,
                "1800000.00",
                "950000.00",
                "250000.00",
                CreditRiskBand.MEDIUM));

        assertEquals(LoanDecisionOutcome.APPROVED, assessment.decision());
        assertEquals(new BigDecimal("250000.00"), assessment.approvedAmount());
        assertEquals("DEMO_POLICY_REQUIREMENTS_MET", assessment.reasonCodes().get(0));
    }

    @Test
    void routesHighCreditRiskToManualReview() {
        UnderwritingAssessment assessment = policy.assess(event(
                2,
                8,
                "1800000.00",
                "950000.00",
                "250000.00",
                CreditRiskBand.HIGH));

        assertEquals(LoanDecisionOutcome.MANUAL_REVIEW, assessment.decision());
        assertNull(assessment.approvedAmount());
        assertEquals("HIGH_CREDIT_RISK_BAND", assessment.reasonCodes().get(0));
    }

    @Test
    void routesRequestsAboveTheDemonstrationCapacityToManualReview() {
        UnderwritingAssessment assessment = policy.assess(event(
                2,
                8,
                "100000.00",
                "50000.00",
                "100000.00",
                CreditRiskBand.LOW));

        assertEquals(LoanDecisionOutcome.MANUAL_REVIEW, assessment.decision());
        assertEquals("REQUEST_EXCEEDS_DEMO_CAPACITY", assessment.reasonCodes().get(0));
    }

    @Test
    void declinesApplicationsWithNoReportedRevenue() {
        UnderwritingAssessment assessment = policy.assess(event(
                2,
                8,
                "0.00",
                "950000.00",
                "250000.00",
                CreditRiskBand.LOW));

        assertEquals(LoanDecisionOutcome.DECLINED, assessment.decision());
        assertEquals("NO_REPORTED_REVENUE", assessment.reasonCodes().get(0));
    }

    @Test
    void routesUnknownEventVersionsToManualReview() {
        UnderwritingAssessment assessment = policy.assess(event(
                1,
                8,
                "1800000.00",
                "950000.00",
                "250000.00",
                CreditRiskBand.LOW));

        assertEquals(LoanDecisionOutcome.MANUAL_REVIEW, assessment.decision());
        assertEquals("UNSUPPORTED_APPLICATION_EVENT_VERSION", assessment.reasonCodes().get(0));
    }

    private LoanApplicationEvent event(
            int schemaVersion,
            int yearsInBusiness,
            String annualRevenue,
            String totalAssets,
            String requestedAmount,
            CreditRiskBand creditRiskBand) {
        return new LoanApplicationEvent(
                schemaVersion,
                UUID.randomUUID(),
                LoanApplicationEventType.CREATED,
                Instant.now(),
                42L,
                "Acme Manufacturing LLC",
                "Manufacturing",
                "US",
                yearsInBusiness,
                new BigDecimal(annualRevenue),
                new BigDecimal(totalAssets),
                creditRiskBand,
                "TERM_LOAN",
                new BigDecimal(requestedAmount),
                60,
                "Equipment purchase",
                "SUBMITTED");
    }
}
