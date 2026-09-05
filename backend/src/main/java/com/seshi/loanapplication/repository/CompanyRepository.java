package com.seshi.loanapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seshi.loanapplication.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}