package com.seshi.loanapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LoanApplicationNotFoundException extends RuntimeException {

    public LoanApplicationNotFoundException(Long id) {
        super("Loan application " + id + " was not found");
    }
}
