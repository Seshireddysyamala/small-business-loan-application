package com.seshi.loanapplication.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Pattern ALLOWED_FORMAT = Pattern.compile("^\\+?[0-9() .-]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String phoneNumber = value.trim();
        if (!ALLOWED_FORMAT.matcher(phoneNumber).matches()) {
            return false;
        }

        long digitCount = phoneNumber.chars().filter(Character::isDigit).count();
        return digitCount >= 10 && digitCount <= 15;
    }
}
