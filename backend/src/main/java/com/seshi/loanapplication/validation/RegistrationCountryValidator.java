package com.seshi.loanapplication.validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationCountryValidator
        implements ConstraintValidator<ValidRegistrationCountry, String> {

    private static final Set<String> ISO_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toUnmodifiableSet());

    private final CountryEligibilityPolicy countryEligibilityPolicy;

    public RegistrationCountryValidator(CountryEligibilityPolicy countryEligibilityPolicy) {
        this.countryEligibilityPolicy = countryEligibilityPolicy;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String countryCode = value.trim().toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRY_CODES.contains(countryCode)) {
            replaceMessage(context, "Registration country must be a valid ISO 3166-1 two-letter code");
            return false;
        }

        if (countryEligibilityPolicy.isRestricted(countryCode)) {
            replaceMessage(
                    context,
                    "Applications from this registration country require manual compliance review and cannot be submitted online");
            return false;
        }

        return true;
    }

    private void replaceMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
