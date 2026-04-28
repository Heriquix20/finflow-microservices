package com.finflow.income.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed.",
                "One or more request fields are invalid.",
                "VALIDATION_ERROR",
                URI.create("https://finflow/errors/validation"),
                request
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed.",
                "One or more request parameters are invalid.",
                "VALIDATION_ERROR",
                URI.create("https://finflow/errors/validation"),
                request
        );

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String errorCode = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "REQUEST_ERROR";
        return buildProblemDetail(
                status,
                "Request could not be processed.",
                ex.getReason(),
                errorCode,
                URI.create("https://finflow/errors/request"),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                "An unexpected error occurred.",
                "INTERNAL_ERROR",
                URI.create("https://finflow/errors/internal"),
                request
        );
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String title,
            String detail,
            String errorCode,
            URI type,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setType(type);
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", OffsetDateTime.now().toString());
        problemDetail.setProperty("path", request != null ? request.getRequestURI() : "n/a");
        return problemDetail;
    }
}
