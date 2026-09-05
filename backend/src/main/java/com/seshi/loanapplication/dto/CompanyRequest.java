package com.seshi.loanapplication.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.seshi.loanapplication.validation.ValidPersonName;
import com.seshi.loanapplication.validation.ValidPhoneNumber;
import com.seshi.loanapplication.validation.ValidRegistrationCountry;
import com.seshi.loanapplication.validation.ValidSsn;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(max = 150, message = "Company name must be 150 characters or fewer")
        String companyName,

        @NotBlank(message = "Business structure is required")
        @Size(max = 50, message = "Business structure must be 50 characters or fewer")
        String businessStructure,

        @NotBlank(message = "Industry is required")
        @Size(max = 100, message = "Industry must be 100 characters or fewer")
        String industry,

        @NotBlank(message = "Registration country is required")
        @ValidRegistrationCountry
        String countryOfRegistration,

        @NotNull(message = "Years in business is required")
        @Min(value = 0, message = "Years in business cannot be negative")
        @Max(value = 200, message = "Years in business cannot exceed 200")
        Integer yearsInBusiness,

        @NotNull(message = "Annual revenue is required")
        @DecimalMin(value = "0.00", message = "Annual revenue cannot be negative")
        @Digits(integer = 17, fraction = 2, message = "Annual revenue must have at most 17 whole digits and 2 decimal places")
        BigDecimal annualRevenue,

        @NotNull(message = "Total assets is required")
        @DecimalMin(value = "0.00", message = "Total assets cannot be negative")
        @Digits(integer = 17, fraction = 2, message = "Total assets must have at most 17 whole digits and 2 decimal places")
        BigDecimal totalAssets,

        @NotBlank(message = "Loan type is required")
        @Size(max = 60, message = "Loan type must be 60 characters or fewer")
        String loanType,

        @NotNull(message = "Requested loan amount is required")
        @DecimalMin(value = "1000.00", message = "Requested loan amount must be at least 1000.00")
        @Digits(integer = 17, fraction = 2, message = "Requested loan amount must have at most 17 whole digits and 2 decimal places")
        BigDecimal requestedLoanAmount,

        @NotNull(message = "Requested term is required")
        @Min(value = 6, message = "Requested term must be at least 6 months")
        @Max(value = 360, message = "Requested term cannot exceed 360 months")
        Integer requestedTermMonths,

        @NotBlank(message = "Loan purpose is required")
        @Size(max = 100, message = "Loan purpose must be 100 characters or fewer")
        String loanPurpose,

        @NotNull(message = "Credit score is required")
        @Min(value = 300, message = "Credit score must be at least 300")
        @Max(value = 850, message = "Credit score cannot exceed 850")
        Integer creditScore,

        @NotBlank(message = "Contact name is required")
        @ValidPersonName
        @Size(max = 120, message = "Contact name must be 120 characters or fewer")
        String contactName,

        @NotBlank(message = "SSN is required")
        @ValidSsn
        String ssn,

        @NotBlank(message = "Contact email is required")
        @Email(message = "Contact email must be valid")
        @Pattern(
                regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$",
                message = "Contact email must include a valid domain")
        @Size(max = 150, message = "Contact email must be 150 characters or fewer")
        String contactEmail,

        @NotBlank(message = "Contact phone is required")
        @ValidPhoneNumber
        @Size(max = 30, message = "Contact phone must be 30 characters or fewer")
        String contactPhone) {

    @Override
    public String toString() {
        return "CompanyRequest[companyName=" + companyName
                + ", countryOfRegistration=" + countryOfRegistration
                + ", identityAndContact=REDACTED]";
    }
}
