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
@Constraint(validatedBy = PersonNameValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE, RECORD_COMPONENT })
@Retention(RUNTIME)
public @interface ValidPersonName {

    String message() default "Contact name may contain only letters, spaces, apostrophes, periods, and hyphens";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
