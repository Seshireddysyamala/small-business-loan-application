package com.seshi.underwriting.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seshi.underwriting.domain.UnderwritingAssessment;
import com.seshi.underwriting.domain.UnderwritingPolicy;
import com.seshi.underwriting.entity.UnderwritingDecision;
import com.seshi.underwriting.messaging.event.LoanApplicationEvent;
import com.seshi.underwriting.messaging.event.LoanDecisionEvent;
import com.seshi.underwriting.repository.UnderwritingDecisionRepository;

@Service
public class UnderwritingDecisionService {

    private static final Logger log = LoggerFactory.getLogger(UnderwritingDecisionService.class);

    private final UnderwritingDecisionRepository repository;
    private final UnderwritingPolicy policy;
    private final Clock clock;

    @Autowired
    public UnderwritingDecisionService(
            UnderwritingDecisionRepository repository,
            UnderwritingPolicy policy) {
        this(repository, policy, Clock.systemUTC());
    }

    UnderwritingDecisionService(
            UnderwritingDecisionRepository repository,
            UnderwritingPolicy policy,
            Clock clock) {
        this.repository = repository;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional
    public LoanDecisionEvent decide(LoanApplicationEvent event) {
        Objects.requireNonNull(event.eventId(), "Application event ID is required");
        Objects.requireNonNull(event.applicationId(), "Application ID is required");

        return repository.findBySourceEventId(event.eventId().toString())
                .map(existing -> {
                    log.info(
                            "Returning existing underwriting decision: sourceEventId={}, decisionId={}, applicationId={}",
                            event.eventId(),
                            existing.getDecisionId(),
                            existing.getApplicationId());
                    return toEvent(existing);
                })
                .orElseGet(() -> createDecision(event));
    }

    private LoanDecisionEvent createDecision(LoanApplicationEvent event) {
        UnderwritingAssessment assessment = policy.assess(event);
        UnderwritingDecision decision = UnderwritingDecision.create(
                event,
                assessment,
                UnderwritingPolicy.VERSION,
                Instant.now(clock));
        return toEvent(repository.save(decision));
    }

    private LoanDecisionEvent toEvent(UnderwritingDecision decision) {
        List<String> reasonCodes = Arrays.stream(decision.getReasonCodes().split(","))
                .filter(reason -> !reason.isBlank())
                .toList();

        return new LoanDecisionEvent(
                1,
                UUID.fromString(decision.getDecisionId()),
                UUID.fromString(decision.getSourceEventId()),
                decision.getApplicationId(),
                decision.getDecision(),
                decision.getApprovedAmount(),
                reasonCodes,
                decision.getPolicyVersion(),
                decision.getDecidedAt());
    }
}
