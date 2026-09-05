package com.seshi.loanapplication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;
import com.seshi.loanapplication.exception.LoanApplicationNotFoundException;
import com.seshi.loanapplication.mapper.CompanyMapper;
import com.seshi.loanapplication.model.Company;
import com.seshi.loanapplication.repository.CompanyRepository;

@Service
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    public List<CompanyResponse> getCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = companyMapper.toEntity(request);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toResponse(savedCompany);
    }

    @Override
    public CompanyResponse getCompanyById(Long id) {
        return companyMapper.toResponse(findCompany(id));
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = findCompany(id);
        companyMapper.updateEntity(company, request);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toResponse(savedCompany);
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException(id));
    }
}
