package com.expense.segmentation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenUtil jwtTokenUtil;

    @Mock private UserDetailsService userDetailsService;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails =
                User.builder()
                        .username("test@example.com")
                        .password("password")
                        .authorities(
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                        .build();
    }

    @Test
    void shouldNotFilter_WithRegisterPath_ShouldReturnTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/auth/register");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_WithLoginPath_ShouldReturnTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/auth/login");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_WithHealthPath_ShouldReturnTrue() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/health");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_WithProtectedPath_ShouldReturnFalse() throws ServletException {
        // Given
        when(request.getServletPath()).thenReturn("/api/protected");

        // When
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication()
            throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication()
            throws ServletException, IOException {
        // Given
        String token = "invalid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(token, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMissingAuthorizationHeader_ShouldNotSetAuthentication()
            throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidAuthorizationHeaderFormat_ShouldNotSetAuthentication()
            throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithTokenExtractionException_ShouldNotSetAuthentication()
            throws ServletException, IOException {
        // Given
        String token = "malformed-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenAlreadyAuthenticated_ShouldNotReAuthenticate()
            throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenUtil.extractUsername(token)).thenReturn("test@example.com");

        // Set existing authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                existingAuth =
                        new org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtil, never()).validateToken(anyString(), any());
        verify(filterChain).doFilter(request, response);
    }
}
