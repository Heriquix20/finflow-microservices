package com.finflow.income.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void handleConstraintViolationExceptionShouldReturnValidationProblem() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("getMonthlySummary.month");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be less than or equal to 12");
        when(request.getRequestURI()).thenReturn("/incomes/summary");

        ProblemDetail problemDetail = handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of(violation)),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation failed.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "VALIDATION_ERROR");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/incomes/summary");
        assertThat(problemDetail.getProperties()).containsKey("errors");
    }

    @Test
    void handleResponseStatusExceptionShouldReturnRequestProblem() {
        when(request.getRequestURI()).thenReturn("/incomes/missing");
        ProblemDetail problemDetail = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found."),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Request could not be processed.");
        assertThat(problemDetail.getDetail()).isEqualTo("Income not found.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "RESOURCE_NOT_FOUND");
    }

    @Test
    void handleGenericExceptionShouldReturnInternalProblem() {
        when(request.getRequestURI()).thenReturn("/incomes");
        ProblemDetail problemDetail = handler.handleGenericException(new RuntimeException("boom"), request);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Internal server error.");
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "INTERNAL_ERROR");
    }

    @Test
    void handleMethodNotSupportedExceptionShouldReturnMethodNotAllowedProblem() {
        when(request.getRequestURI()).thenReturn("/incomes");
        ProblemDetail problemDetail = handler.handleMethodNotSupportedException(
                new HttpRequestMethodNotSupportedException("PATCH"),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(problemDetail.getTitle()).isEqualTo("HTTP method not allowed.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "METHOD_NOT_ALLOWED");
    }

    @Test
    void handleMessageNotReadableExceptionShouldReturnMalformedRequestProblem() {
        when(request.getRequestURI()).thenReturn("/incomes");
        ProblemDetail problemDetail = handler.handleMessageNotReadableException(
                new HttpMessageNotReadableException("body missing"),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Malformed request body.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "MALFORMED_REQUEST");
    }

    @Test
    void handleMediaTypeNotSupportedExceptionShouldReturnUnsupportedMediaTypeProblem() {
        when(request.getRequestURI()).thenReturn("/incomes");
        ProblemDetail problemDetail = handler.handleMediaTypeNotSupportedException(
                new HttpMediaTypeNotSupportedException("text/plain"),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Unsupported media type.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "UNSUPPORTED_MEDIA_TYPE");
    }
}
