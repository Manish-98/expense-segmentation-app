package com.expense.segmentation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/path");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void handleBadCredentialsException_ShouldReturnUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAuthenticationExceptions(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleUsernameNotFoundException_ShouldReturnUnauthorized() {
        // Given
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAuthenticationExceptions(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Failed");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAccessDeniedException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Access Denied");
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to access this resource");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequest() {
        // Given
        RuntimeException exception = new RuntimeException("Something went wrong");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleRuntimeException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new IOException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGeneralException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleGeneralException_WithNullPointerException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGeneralException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getPath()).isEqualTo("/test/path");
    }
}
