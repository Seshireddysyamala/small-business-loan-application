package com.seshi.loanapplication.messaging.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seshi.loanapplication.entity.Company;
import com.seshi.loanapplication.messaging.event.LoanDecisionEvent;
import com.seshi.loanapplication.messaging.event.LoanDecisionOutcome;
import com.seshi.loanapplication.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class LoanDecisionEventHandlerTests {

    @Mock
    private CompanyRepository companyRepository;

    @Test
    void appliesTheUnderwritingDecisionToTheApplication() {
        Company company = new Company();
        company.setId(42L);
        company.markSubmitted();
        LoanDecisionEvent event = new LoanDecisionEvent(
                1,
                UUID.randomUUID(),
                UUID.randomUUID(),
                42L,
                LoanDecisionOutcome.MANUAL_REVIEW,
                null,
                List.of("HIGH_CREDIT_RISK"),
                "demo-policy-v1",
                Instant.now());
        when(companyRepository.findById(42L)).thenReturn(Optional.of(company));

        new LoanDecisionEventHandler(companyRepository).apply(event);

        assertEquals("MANUAL_REVIEW", company.getApplicationStatus());
        verify(companyRepository).save(company);
    }
}
