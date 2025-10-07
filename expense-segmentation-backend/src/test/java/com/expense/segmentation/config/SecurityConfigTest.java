package com.expense.segmentation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(UserDetailsService.class);
        jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_ShouldEncodePasswordCorrectly() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void authenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isNotNull();
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    void authenticationProvider_ShouldBeConfiguredWithUserDetailsService() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isNotNull();
        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) authProvider;
        assertThat(daoProvider).isNotNull();
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        // Given
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        org.mockito.Mockito.when(config.getAuthenticationManager()).thenReturn(expectedManager);

        // When
        AuthenticationManager authenticationManager = securityConfig.authenticationManager(config);

        // Then
        assertThat(authenticationManager).isNotNull();
        assertThat(authenticationManager).isEqualTo(expectedManager);
    }

    @Test
    void corsConfigurationSource_ShouldReturnCorsConfigurationSource() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

        // Then
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    void corsConfigurationSource_ShouldAllowAllOrigins() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig =
                corsConfigurationSource.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest());

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("*");
    }

    @Test
    void corsConfigurationSource_ShouldAllowAllHeaders() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig =
                corsConfigurationSource.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest());

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
    }

    @Test
    void corsConfigurationSource_ShouldAllowStandardHttpMethods() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig =
                corsConfigurationSource.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest());

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedMethods())
                .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void corsConfigurationSource_ShouldExposeAuthorizationHeader() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfig =
                corsConfigurationSource.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest());

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getExposedHeaders()).contains("Authorization");
    }
}
