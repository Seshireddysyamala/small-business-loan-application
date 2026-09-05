package com.seshi.loanapplication.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SsnValidator implements ConstraintValidator<ValidSsn, String> {

    private static final Pattern VALID_FORMAT = Pattern.compile("^(?:\\d{9}|\\d{3}-\\d{2}-\\d{4})$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String ssn = value.trim();
        if (!VALID_FORMAT.matcher(ssn).matches()) {
            return false;
        }

        String digits = ssn.replace("-", "");
        int areaNumber = Integer.parseInt(digits.substring(0, 3));
        String groupNumber = digits.substring(3, 5);
        String serialNumber = digits.substring(5);

        return areaNumber != 0
                && areaNumber != 666
                && areaNumber < 900
                && !groupNumber.equals("00")
                && !serialNumber.equals("0000");
    }
}
