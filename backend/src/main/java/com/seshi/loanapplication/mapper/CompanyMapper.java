package com.seshi.loanapplication.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;
import com.seshi.loanapplication.model.Company;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequest request) {
        Company company = new Company();
        updateEntity(company, request);
        return company;
    }

    public void updateEntity(Company company, CompanyRequest request) {
        company.setCompanyName(clean(request.companyName()));
        company.setBusinessStructure(clean(request.businessStructure()));
        company.setIndustry(clean(request.industry()));
        company.setCountryOfRegistration(request.countryOfRegistration().trim().toUpperCase(Locale.ROOT));
        company.setYearsInBusiness(request.yearsInBusiness());
        company.setAnnualRevenue(request.annualRevenue());
        company.setTotalAssets(request.totalAssets());
        company.setLoanType(clean(request.loanType()));
        company.setRequestedLoanAmount(request.requestedLoanAmount());
        company.setRequestedTermMonths(request.requestedTermMonths());
        company.setLoanPurpose(clean(request.loanPurpose()));
        company.setCreditScore(request.creditScore());
        company.setContactName(clean(request.contactName()));
        company.setSsnLastFour(ssnLastFour(request.ssn()));
        company.setContactEmail(clean(request.contactEmail()).toLowerCase(Locale.ROOT));
        company.setContactPhone(clean(request.contactPhone()));
    }

    public CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getCompanyName(),
                company.getBusinessStructure(),
                company.getIndustry(),
                company.getCountryOfRegistration(),
                company.getYearsInBusiness(),
                company.getAnnualRevenue(),
                company.getTotalAssets(),
                company.getLoanType(),
                company.getRequestedLoanAmount(),
                company.getRequestedTermMonths(),
                company.getLoanPurpose(),
                company.getCreditScore(),
                company.getContactName(),
                company.getSsnLastFour(),
                company.getContactEmail(),
                company.getContactPhone(),
                company.getApplicationStatus(),
                company.getSubmittedAt(),
                company.getUpdatedAt());
    }

    private String clean(String value) {
        return value.trim();
    }

    private String ssnLastFour(String ssn) {
        String digits = ssn.replace("-", "");
        return digits.substring(digits.length() - 4);
    }
}
