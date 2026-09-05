package com.seshi.loanapplication.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PersonNameValidator implements ConstraintValidator<ValidPersonName, String> {

    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[\\p{L}\\p{M} .'-]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String name = value.trim();
        long letterCount = name.codePoints().filter(Character::isLetter).count();
        return letterCount >= 2
                && ALLOWED_CHARACTERS.matcher(name).matches()
                && Character.isLetter(name.codePointAt(0))
                && Character.isLetter(name.codePointBefore(name.length()));
    }
}
