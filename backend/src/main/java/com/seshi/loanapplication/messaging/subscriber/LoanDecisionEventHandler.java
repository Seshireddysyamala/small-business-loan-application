package com.seshi.loanapplication.messaging.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seshi.loanapplication.exception.LoanApplicationNotFoundException;
import com.seshi.loanapplication.entity.Company;
import com.seshi.loanapplication.messaging.event.LoanDecisionEvent;
import com.seshi.loanapplication.repository.CompanyRepository;

@Service
public class LoanDecisionEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LoanDecisionEventHandler.class);

    private final CompanyRepository companyRepository;

    public LoanDecisionEventHandler(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void apply(LoanDecisionEvent event) {
        Company company = companyRepository.findById(event.applicationId())
                .orElseThrow(() -> new LoanApplicationNotFoundException(event.applicationId()));

        company.applyDecision(event.decision().name());
        companyRepository.save(company);

        log.info(
                "Loan decision applied: decisionId={}, applicationId={}, decision={}",
                event.decisionId(),
                event.applicationId(),
                event.decision());
    }
}
