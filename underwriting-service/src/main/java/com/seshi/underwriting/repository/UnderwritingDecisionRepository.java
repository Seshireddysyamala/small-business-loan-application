package com.seshi.underwriting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seshi.underwriting.entity.UnderwritingDecision;

public interface UnderwritingDecisionRepository extends JpaRepository<UnderwritingDecision, String> {

    Optional<UnderwritingDecision> findBySourceEventId(String sourceEventId);
}
