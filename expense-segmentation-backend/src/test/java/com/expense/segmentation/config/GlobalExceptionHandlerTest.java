package com.expense.segmentation.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentialsException_ShouldReturnUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleAuthenticationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Authentication Failed");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid email or password");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleUsernameNotFoundException_ShouldReturnUnauthorized() {
        // Given
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleAuthenticationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Authentication Failed");
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleAccessDeniedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("error")).isEqualTo("Access Denied");
        assertThat(response.getBody().get("message"))
                .isEqualTo("You do not have permission to access this resource");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequest() {
        // Given
        RuntimeException exception = new RuntimeException("Something went wrong");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Something went wrong");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new IOException("Unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneralException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleGeneralException_WithNullPointerException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneralException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
    }
}
