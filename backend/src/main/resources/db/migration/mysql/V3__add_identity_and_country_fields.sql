DROP PROCEDURE IF EXISTS ensure_identity_and_country_columns;

DELIMITER $$
CREATE PROCEDURE ensure_identity_and_country_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'country_of_registration'
    ) THEN
        ALTER TABLE companies ADD COLUMN country_of_registration VARCHAR(2) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'ssn_last_four'
    ) THEN
        ALTER TABLE companies ADD COLUMN ssn_last_four VARCHAR(4) NULL;
    END IF;
END $$
DELIMITER ;

CALL ensure_identity_and_country_columns();
DROP PROCEDURE ensure_identity_and_country_columns;
