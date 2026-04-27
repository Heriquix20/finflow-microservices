package com.finflow.auth.handler;

import com.finflow.auth.dto.LoginRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private MethodParameter loginRequestParameter;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        Method method = SampleController.class.getDeclaredMethod("login", LoginRequest.class);
        loginRequestParameter = new MethodParameter(method, 0);
    }

    @Test
    void shouldBuildRequestProblemForResponseStatusException() {
        var problemDetail = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.")
        );

        assertThat(problemDetail.getStatus()).isEqualTo(409);
        assertThat(problemDetail.getDetail()).isEqualTo("Email is already registered.");
    }

    @Test
    void shouldBuildInternalProblemForGenericException() {
        var problemDetail = handler.handleGenericException(new RuntimeException("boom"));

        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred.");
    }

    @Test
    void shouldBuildValidationProblemForMethodArgumentException() {
        LoginRequest request = new LoginRequest("invalid-email", "");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "loginRequest");
        Validation.buildDefaultValidatorFactory()
                .getValidator()
                .validate(request)
                .forEach(violation -> bindingResult.rejectValue(
                        violation.getPropertyPath().toString(),
                        "invalid",
                        violation.getMessage()
                ));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(loginRequestParameter, bindingResult);
        var problemDetail = handler.handleValidationException(exception);

        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getProperties()).containsKey("errors");
    }

    private static class SampleController {
        @SuppressWarnings("unused")
        public void login(LoginRequest request) {
        }
    }
}
