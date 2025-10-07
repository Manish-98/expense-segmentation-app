package com.expense.segmentation.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails userDetails;
    private String testSecret;
    private Long testExpiration;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        testSecret = "dGhpc0lzQVNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb24=";
        testExpiration = 3600000L; // 1 hour

        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = jwtTokenUtil.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void extractUsername_FromValidToken_ShouldReturnUsername() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        String username = jwtTokenUtil.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void extractExpiration_FromValidToken_ShouldReturnFutureDate() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        Date expiration = jwtTokenUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // When
        Boolean isValid = jwtTokenUtil.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Given
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 100L); // Very short expiration
        String expiredToken = jwtTokenUtil.generateToken(userDetails);

        // Wait for token to expire
        Thread.sleep(200);

        // Reset expiration to normal
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);

        // When
        Boolean isValid = jwtTokenUtil.validateToken(expiredToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void extractClaim_ShouldExtractSubject() {
        // Given
        String token = jwtTokenUtil.generateToken(userDetails);

        // When
        String subject = jwtTokenUtil.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo("test@example.com");
    }

    @Test
    void extractClaim_ShouldExtractIssuedAt() {
        // Given
        Date beforeGeneration = new Date(System.currentTimeMillis() - 1000); // 1 second buffer
        String token = jwtTokenUtil.generateToken(userDetails);
        Date afterGeneration = new Date(System.currentTimeMillis() + 1000); // 1 second buffer

        // When
        Date issuedAt = jwtTokenUtil.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt).isBetween(beforeGeneration, afterGeneration);
    }

    @Test
    void generateToken_MultipleTokensForSameUser_ShouldHaveDifferentIssuedAt() throws InterruptedException {
        // Given
        String token1 = jwtTokenUtil.generateToken(userDetails);
        Thread.sleep(1100); // Ensure at least 1 second difference
        String token2 = jwtTokenUtil.generateToken(userDetails);

        // When
        Date issuedAt1 = jwtTokenUtil.extractClaim(token1, Claims::getIssuedAt);
        Date issuedAt2 = jwtTokenUtil.extractClaim(token2, Claims::getIssuedAt);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(issuedAt2).isAfterOrEqualTo(issuedAt1);
    }

    @Test
    void extractUsername_FromInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "malformed.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.validateToken(malformedToken, userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectExpirationTime() {
        // Given
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtTokenUtil.generateToken(userDetails);
        long afterGeneration = System.currentTimeMillis();

        // When
        Date expiration = jwtTokenUtil.extractExpiration(token);

        // Then
        long expectedMinExpiration = beforeGeneration + testExpiration - 1000; // 1 second tolerance
        long expectedMaxExpiration = afterGeneration + testExpiration + 1000; // 1 second tolerance
        long actualExpiration = expiration.getTime();

        assertThat(actualExpiration).isBetween(
                expectedMinExpiration,
                expectedMaxExpiration
        );
    }
}
