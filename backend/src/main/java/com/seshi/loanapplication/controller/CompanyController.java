package com.seshi.loanapplication.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PutMapping;

import com.seshi.loanapplication.model.Company;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final List<Company> companies = new ArrayList<>();
    private long nextId = 1;

    @GetMapping
    public List<Company> getCompanies() {
        return companies;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Company createCompany(@RequestBody Company company) {
        company.setId(nextId++);
        companies.add(company);
        return company;
    }

    @GetMapping("/{id}")
    public Company getCompanyById(@PathVariable Long id) {
    return companies.stream()
            .filter(company -> company.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Company not found"
            ));
}
    @PutMapping("/{id}")
    public Company updateCompany(
        @PathVariable Long id,
        @RequestBody Company updatedCompany) {

    Company existingCompany = companies.stream()
            .filter(company -> company.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Company not found"
            ));

    existingCompany.setCompanyName(updatedCompany.getCompanyName());
    existingCompany.setCreditScore(updatedCompany.getCreditScore());
    existingCompany.setTotalAssets(updatedCompany.getTotalAssets());
    existingCompany.setRequestedLoanAmount(
            updatedCompany.getRequestedLoanAmount()
    );

    return existingCompany;
}
}