package com.seshi.loanapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seshi.loanapplication.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
