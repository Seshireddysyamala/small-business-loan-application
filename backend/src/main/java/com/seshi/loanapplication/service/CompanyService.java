package com.seshi.loanapplication.service;

import java.util.List;

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;

public interface CompanyService {

    List<CompanyResponse> getCompanies();

    CompanyResponse createCompany(CompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    CompanyResponse updateCompany(Long id, CompanyRequest request);
}
