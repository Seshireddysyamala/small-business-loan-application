CREATE TABLE companies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(150) NOT NULL,
    credit_score INT NOT NULL,
    total_assets DECIMAL(19, 2) NOT NULL,
    requested_loan_amount DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id)
);
