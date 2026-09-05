package com.seshi.loanapplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;
import com.seshi.loanapplication.exception.LoanApplicationNotFoundException;
import com.seshi.loanapplication.mapper.CompanyMapper;
import com.seshi.loanapplication.model.Company;
import com.seshi.loanapplication.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTests {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyMapper companyMapper;
    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() {
        companyMapper = new CompanyMapper();
        companyService = new CompanyServiceImpl(companyRepository, companyMapper);
    }

    @Test
    void createsApplicationFromRequestAndReturnsServerManagedFields() {
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            assertNull(company.getId());
            company.setId(42L);
            company.markSubmitted();
            return company;
        });

        CompanyResponse response = companyService.createCompany(validRequest());

        assertEquals(42L, response.id());
        assertEquals("Acme Manufacturing LLC", response.companyName());
        assertEquals("US", response.countryOfRegistration());
        assertEquals("6789", response.ssnLastFour());
        assertEquals("SUBMITTED", response.applicationStatus());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void updatesApplicantFieldsWithoutReplacingServerManagedFields() {
        Company existingCompany = companyMapper.toEntity(validRequest());
        existingCompany.setId(7L);
        existingCompany.markSubmitted();

        CompanyRequest updatedRequest = requestWithLoanAmount("325000.00");
        when(companyRepository.findById(7L)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.save(existingCompany)).thenReturn(existingCompany);

        CompanyResponse response = companyService.updateCompany(7L, updatedRequest);

        assertEquals(7L, response.id());
        assertEquals(new BigDecimal("325000.00"), response.requestedLoanAmount());
        assertEquals("SUBMITTED", response.applicationStatus());
        verify(companyRepository).save(existingCompany);
    }

    @Test
    void mapsAllApplicationsToResponseDtos() {
        Company first = companyMapper.toEntity(validRequest());
        first.setId(1L);
        first.markSubmitted();

        Company second = companyMapper.toEntity(requestWithLoanAmount("500000.00"));
        second.setId(2L);
        second.markSubmitted();

        when(companyRepository.findAll()).thenReturn(List.of(first, second));

        List<CompanyResponse> responses = companyService.getCompanies();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(new BigDecimal("500000.00"), responses.get(1).requestedLoanAmount());
    }

    @Test
    void throwsNotFoundWhenApplicationDoesNotExist() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        LoanApplicationNotFoundException exception = assertThrows(
                LoanApplicationNotFoundException.class,
                () -> companyService.getCompanyById(99L));

        assertEquals("Loan application 99 was not found", exception.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
    }

    private CompanyRequest validRequest() {
        return requestWithLoanAmount("250000.00");
    }

    private CompanyRequest requestWithLoanAmount(String requestedLoanAmount) {
        return new CompanyRequest(
                "Acme Manufacturing LLC",
                "LLC",
                "Manufacturing",
                "US",
                8,
                new BigDecimal("1800000.00"),
                new BigDecimal("950000.00"),
                "TERM_LOAN",
                new BigDecimal(requestedLoanAmount),
                60,
                "Equipment purchase",
                735,
                "Jordan Lee",
                "123-45-6789",
                "jordan.lee@example.com",
                "212-555-0147");
    }
}
