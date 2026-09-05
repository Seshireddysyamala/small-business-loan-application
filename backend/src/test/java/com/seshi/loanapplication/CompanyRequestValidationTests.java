package com.seshi.loanapplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.seshi.loanapplication.dto.CompanyRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@SpringBootTest
@ActiveProfiles("test")
class CompanyRequestValidationTests {

    @Autowired
    private Validator validator;

    @Test
    void acceptsACompleteApplication() {
        assertTrue(validator.validate(validRequest()).isEmpty());
    }

    @Test
    void rejectsRestrictedCountryAndInvalidIdentityContactFields() {
        CompanyRequest request = new CompanyRequest(
                "Acme Manufacturing LLC",
                "Limited liability company",
                "Manufacturing",
                "IR",
                8,
                new BigDecimal("1800000.00"),
                new BigDecimal("950000.00"),
                "Business term loan",
                new BigDecimal("250000.00"),
                60,
                "Equipment purchase",
                735,
                "Jordan 123",
                "000-12-3456",
                "not-an-email",
                "555-12");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);
        Set<String> invalidFields = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "countryOfRegistration",
                "contactName",
                "ssn",
                "contactEmail",
                "contactPhone"), invalidFields);
    }

    @Test
    void acceptsLowercaseCountryAndInternationalContactData() {
        CompanyRequest request = new CompanyRequest(
                "Café Consulting LLC",
                "Limited liability company",
                "Professional services",
                "fr",
                3,
                new BigDecimal("450000.00"),
                new BigDecimal("150000.00"),
                "Business line of credit",
                new BigDecimal("50000.00"),
                24,
                "Working capital",
                720,
                "Élodie D'Arcy",
                "123456789",
                "elodie@example.fr",
                "+33 1 42 68 53 00");

        assertTrue(validator.validate(request).isEmpty());
    }

    private CompanyRequest validRequest() {
        return new CompanyRequest(
                "Acme Manufacturing LLC",
                "Limited liability company",
                "Manufacturing",
                "US",
                8,
                new BigDecimal("1800000.00"),
                new BigDecimal("950000.00"),
                "Business term loan",
                new BigDecimal("250000.00"),
                60,
                "Equipment purchase",
                735,
                "Jordan Lee",
                "123-45-6789",
                "jordan.lee@example.com",
                "212-555-0147");
    }
}
