package com.seshi.loanapplication.validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CountryEligibilityPolicy {

    private final Set<String> restrictedCountryCodes;

    public CountryEligibilityPolicy(
            @Value("${loan.validation.restricted-country-codes:CU,IR,KP}") String configuredCodes) {
        restrictedCountryCodes = Arrays.stream(configuredCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isRestricted(String countryCode) {
        return restrictedCountryCodes.contains(countryCode.toUpperCase(Locale.ROOT));
    }
}
