package com.finflow.gateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        return buildValidationProblem(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationException(WebExchangeBindException ex) {
        return buildValidationProblem(ex.getBindingResult().getFieldErrors());
    }

    private ProblemDetail buildValidationProblem(List<FieldError> fieldErrors) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed.");
        problemDetail.setDetail("One or more request fields are invalid.");
        problemDetail.setType(URI.create("https://finflow/errors/validation"));

        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.valueOf(ex.getStatusCode().value()));
        problemDetail.setTitle("Request could not be processed.");
        problemDetail.setDetail(ex.getReason());
        problemDetail.setType(URI.create("https://finflow/errors/request"));
        return problemDetail;
    }
}
