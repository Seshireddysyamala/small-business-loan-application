ALTER TABLE companies ADD COLUMN business_structure VARCHAR(50);
ALTER TABLE companies ADD COLUMN industry VARCHAR(100);
ALTER TABLE companies ADD COLUMN years_in_business INT;
ALTER TABLE companies ADD COLUMN annual_revenue DECIMAL(19, 2);
ALTER TABLE companies ADD COLUMN loan_type VARCHAR(60);
ALTER TABLE companies ADD COLUMN requested_term_months INT;
ALTER TABLE companies ADD COLUMN loan_purpose VARCHAR(100);
ALTER TABLE companies ADD COLUMN contact_name VARCHAR(120);
ALTER TABLE companies ADD COLUMN contact_email VARCHAR(150);
ALTER TABLE companies ADD COLUMN contact_phone VARCHAR(30);
ALTER TABLE companies ADD COLUMN application_status VARCHAR(30);
ALTER TABLE companies ADD COLUMN submitted_at TIMESTAMP(6);
ALTER TABLE companies ADD COLUMN updated_at TIMESTAMP(6);

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
    submitted_at = COALESCE(submitted_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, submitted_at, CURRENT_TIMESTAMP);

ALTER TABLE companies ALTER COLUMN business_structure SET NOT NULL;
ALTER TABLE companies ALTER COLUMN industry SET NOT NULL;
ALTER TABLE companies ALTER COLUMN years_in_business SET NOT NULL;
ALTER TABLE companies ALTER COLUMN annual_revenue SET NOT NULL;
ALTER TABLE companies ALTER COLUMN loan_type SET NOT NULL;
ALTER TABLE companies ALTER COLUMN requested_term_months SET NOT NULL;
ALTER TABLE companies ALTER COLUMN loan_purpose SET NOT NULL;
ALTER TABLE companies ALTER COLUMN contact_name SET NOT NULL;
ALTER TABLE companies ALTER COLUMN contact_email SET NOT NULL;
ALTER TABLE companies ALTER COLUMN contact_phone SET NOT NULL;
ALTER TABLE companies ALTER COLUMN application_status SET NOT NULL;
ALTER TABLE companies ALTER COLUMN submitted_at SET NOT NULL;
ALTER TABLE companies ALTER COLUMN updated_at SET NOT NULL;
