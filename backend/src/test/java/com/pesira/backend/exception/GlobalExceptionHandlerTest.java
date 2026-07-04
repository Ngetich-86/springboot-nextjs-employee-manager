package com.pesira.backend.exception;

import com.pesira.backend.dto.ApiResponse;
import com.pesira.backend.dto.ValidationErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundExceptionReturns404() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
    }

    @Test
    void handleAuthenticationExceptionReturns401() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(
                new BadCredentialsException("Invalid credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
    }

    @Test
    void handleAccessDeniedExceptionReturns403() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(
                new AccessDeniedException("Forbidden"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    void handleValidationExceptionReturnsFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "Email is required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                null,
                bindingResult);

        ResponseEntity<ApiResponse<List<ValidationErrorDto>>> response =
                handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().getFirst().getField()).isEqualTo("email");
    }
}
