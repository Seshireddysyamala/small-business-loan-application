CREATE TABLE IF NOT EXISTS companies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(150) NOT NULL,
    credit_score INT NOT NULL,
    total_assets DECIMAL(19, 2) NOT NULL,
    requested_loan_amount DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id)
);

DROP PROCEDURE IF EXISTS ensure_loan_application_columns;

DELIMITER $$
CREATE PROCEDURE ensure_loan_application_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'business_structure'
    ) THEN
        ALTER TABLE companies ADD COLUMN business_structure VARCHAR(50) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'industry'
    ) THEN
        ALTER TABLE companies ADD COLUMN industry VARCHAR(100) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'years_in_business'
    ) THEN
        ALTER TABLE companies ADD COLUMN years_in_business INT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'annual_revenue'
    ) THEN
        ALTER TABLE companies ADD COLUMN annual_revenue DECIMAL(19, 2) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'loan_type'
    ) THEN
        ALTER TABLE companies ADD COLUMN loan_type VARCHAR(60) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'requested_term_months'
    ) THEN
        ALTER TABLE companies ADD COLUMN requested_term_months INT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'loan_purpose'
    ) THEN
        ALTER TABLE companies ADD COLUMN loan_purpose VARCHAR(100) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'contact_name'
    ) THEN
        ALTER TABLE companies ADD COLUMN contact_name VARCHAR(120) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'contact_email'
    ) THEN
        ALTER TABLE companies ADD COLUMN contact_email VARCHAR(150) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'contact_phone'
    ) THEN
        ALTER TABLE companies ADD COLUMN contact_phone VARCHAR(30) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'application_status'
    ) THEN
        ALTER TABLE companies ADD COLUMN application_status VARCHAR(30) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'submitted_at'
    ) THEN
        ALTER TABLE companies ADD COLUMN submitted_at DATETIME(6) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE companies ADD COLUMN updated_at DATETIME(6) NULL;
    END IF;
END $$
DELIMITER ;

CALL ensure_loan_application_columns();
DROP PROCEDURE ensure_loan_application_columns;

UPDATE companies
SET business_structure = COALESCE(business_structure, 'OTHER'),
    industry = COALESCE(industry, 'Not provided'),
    years_in_business = COALESCE(years_in_business, 0),
    annual_revenue = COALESCE(annual_revenue, total_assets, 0.00),
    loan_type = COALESCE(loan_type, 'TERM_LOAN'),
    requested_term_months = COALESCE(requested_term_months, 12),
    loan_purpose = COALESCE(loan_purpose, 'OTHER'),
    contact_name = COALESCE(contact_name, 'Migration required'),
    contact_email = COALESCE(contact_email, 'migration-required@example.invalid'),
    contact_phone = COALESCE(contact_phone, 'Not provided'),
    application_status = COALESCE(application_status, 'SUBMITTED'),
    submitted_at = COALESCE(submitted_at, CURRENT_TIMESTAMP(6)),
    updated_at = COALESCE(updated_at, submitted_at, CURRENT_TIMESTAMP(6));

ALTER TABLE companies
    MODIFY COLUMN company_name VARCHAR(150) NOT NULL,
    MODIFY COLUMN business_structure VARCHAR(50) NOT NULL,
    MODIFY COLUMN industry VARCHAR(100) NOT NULL,
    MODIFY COLUMN years_in_business INT NOT NULL,
    MODIFY COLUMN annual_revenue DECIMAL(19, 2) NOT NULL,
    MODIFY COLUMN total_assets DECIMAL(19, 2) NOT NULL,
    MODIFY COLUMN loan_type VARCHAR(60) NOT NULL,
    MODIFY COLUMN requested_loan_amount DECIMAL(19, 2) NOT NULL,
    MODIFY COLUMN requested_term_months INT NOT NULL,
    MODIFY COLUMN loan_purpose VARCHAR(100) NOT NULL,
    MODIFY COLUMN credit_score INT NOT NULL,
    MODIFY COLUMN contact_name VARCHAR(120) NOT NULL,
    MODIFY COLUMN contact_email VARCHAR(150) NOT NULL,
    MODIFY COLUMN contact_phone VARCHAR(30) NOT NULL,
    MODIFY COLUMN application_status VARCHAR(30) NOT NULL,
    MODIFY COLUMN submitted_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;
