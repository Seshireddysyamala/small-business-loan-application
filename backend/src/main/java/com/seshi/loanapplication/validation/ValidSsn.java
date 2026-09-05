package com.seshi.loanapplication.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = SsnValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE, RECORD_COMPONENT })
@Retention(RUNTIME)
public @interface ValidSsn {

    String message() default "SSN must be 9 digits in XXX-XX-XXXX format and use valid number ranges";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
