package com.seshi.loanapplication.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.seshi.loanapplication.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return error(
                HttpStatus.BAD_REQUEST,
                "The application contains invalid or missing fields",
                request,
                fieldErrors);
    }

    @ExceptionHandler(LoanApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(
            LoanApplicationNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Request body is missing, malformed, or contains a value with the wrong type",
                request,
                Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Path parameter '" + exception.getName() + "' has an invalid value",
                request,
                Map.of(exception.getName(), "A numeric application id is required"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        LOGGER.warn("Database rejected loan application data for {}", request.getRequestURI());
        return error(
                HttpStatus.CONFLICT,
                "The application conflicts with an existing record or database rule",
                request,
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unexpected error while processing {}", request.getRequestURI(), exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later",
                request,
                Map.of());
    }

    private ApiErrorResponse error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
    }
}
