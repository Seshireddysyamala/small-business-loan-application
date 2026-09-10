package com.seshi.loanapplication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seshi.loanapplication.dto.CompanyRequest;
import com.seshi.loanapplication.dto.CompanyResponse;
import com.seshi.loanapplication.exception.LoanApplicationNotFoundException;
import com.seshi.loanapplication.mapper.CompanyMapper;
import com.seshi.loanapplication.entity.Company;
import com.seshi.loanapplication.messaging.event.LoanApplicationEvent;
import com.seshi.loanapplication.messaging.event.LoanApplicationEventType;
import com.seshi.loanapplication.messaging.publisher.LoanApplicationEventPublisher;
import com.seshi.loanapplication.repository.CompanyRepository;

@Service
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final LoanApplicationEventPublisher eventPublisher;

    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            CompanyMapper companyMapper,
            LoanApplicationEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.eventPublisher = eventPublisher;
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
        CompanyResponse response = companyMapper.toResponse(savedCompany);
        eventPublisher.publish(LoanApplicationEvent.from(response, LoanApplicationEventType.CREATED));
        return response;
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
        CompanyResponse response = companyMapper.toResponse(savedCompany);
        eventPublisher.publish(LoanApplicationEvent.from(response, LoanApplicationEventType.UPDATED));
        return response;
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException(id));
    }
}
