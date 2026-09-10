package com.seshi.underwriting.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.seshi.underwriting.domain.UnderwritingAssessment;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionOutcome;

@Entity
@Table(
        name = "underwriting_decisions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_underwriting_source_event",
                columnNames = "source_event_id"))
public class UnderwritingDecision {

    @Id
    @Column(name = "decision_id", length = 36, nullable = false)
    private String decisionId;

    @Column(name = "source_event_id", length = 36, nullable = false)
    private String sourceEventId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanDecisionOutcome decision;

    @Column(name = "approved_amount", precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "reason_codes", nullable = false, length = 500)
    private String reasonCodes;

    @Column(name = "policy_version", nullable = false, length = 50)
    private String policyVersion;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected UnderwritingDecision() {
    }

    public static UnderwritingDecision create(
            LoanApplicationEvent sourceEvent,
            UnderwritingAssessment assessment,
            String policyVersion,
            Instant decidedAt) {
        UnderwritingDecision decision = new UnderwritingDecision();
        decision.decisionId = UUID.randomUUID().toString();
        decision.sourceEventId = sourceEvent.eventId().toString();
        decision.applicationId = sourceEvent.applicationId();
        decision.decision = assessment.decision();
        decision.approvedAmount = assessment.approvedAmount();
        decision.reasonCodes = String.join(",", assessment.reasonCodes());
        decision.policyVersion = policyVersion;
        decision.decidedAt = decidedAt;
        return decision;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public LoanDecisionOutcome getDecision() {
        return decision;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public String getReasonCodes() {
        return reasonCodes;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
