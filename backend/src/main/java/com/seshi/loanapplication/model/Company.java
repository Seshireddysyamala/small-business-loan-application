package com.seshi.loanapplication.model;

import java.math.BigDecimal;

public class Company {

    private Long id;
    private String companyName;
    private Integer creditScore;
    private BigDecimal totalAssets;
    private BigDecimal requestedLoanAmount;

    public Company() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getRequestedLoanAmount() {
        return requestedLoanAmount;
    }

    public void setRequestedLoanAmount(BigDecimal requestedLoanAmount) {
        this.requestedLoanAmount = requestedLoanAmount;
    }
}