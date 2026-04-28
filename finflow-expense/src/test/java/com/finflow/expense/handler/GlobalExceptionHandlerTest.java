package com.finflow.expense.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
        when(request.getRequestURI()).thenReturn("/expenses/summary");

        ProblemDetail problemDetail = handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of(violation)),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation failed.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "VALIDATION_ERROR");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/expenses/summary");
        assertThat(problemDetail.getProperties()).containsKey("errors");
    }

    @Test
    void handleResponseStatusExceptionShouldReturnRequestProblem() {
        when(request.getRequestURI()).thenReturn("/expenses/missing");
        ProblemDetail problemDetail = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."),
                request
        );

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Request could not be processed.");
        assertThat(problemDetail.getDetail()).isEqualTo("Expense not found.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "RESOURCE_NOT_FOUND");
    }

    @Test
    void handleGenericExceptionShouldReturnInternalProblem() {
        when(request.getRequestURI()).thenReturn("/expenses");
        ProblemDetail problemDetail = handler.handleGenericException(new RuntimeException("boom"), request);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Internal server error.");
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "INTERNAL_ERROR");
    }
}
