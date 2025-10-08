package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.AuthResponse;
import com.expense.segmentation.dto.LoginRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.UserMapper;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtTokenUtil jwtTokenUtil;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks private AuthService authService;

    private UserMapper userMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role employeeRole;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Initialize real mapper
        userMapper = new UserMapper();
        authService =
                new AuthService(
                        userRepository,
                        roleRepository,
                        passwordEncoder,
                        jwtTokenUtil,
                        authenticationManager,
                        userDetailsService,
                        userMapper);

        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");

        employeeRole = new Role();
        employeeRole.setId(UUID.randomUUID());
        employeeRole.setName(RoleType.EMPLOYEE);
        employeeRole.setDescription("Employee role");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setRole(employeeRole);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities(
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                        .build();
    }

    @Test
    void register_WithValidRequest_ShouldCreateUserAndReturnAuthResponse() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleType.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getUser().getName()).isEqualTo("John Doe");
        assertThat(response.getUser().getRole()).isEqualTo(RoleType.EMPLOYEE);

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(RoleType.EMPLOYEE);
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateToken(userDetails);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenEmployeeRoleNotFound_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleType.EMPLOYEE)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(RoleType.EMPLOYEE);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("test-jwt-token");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtTokenUtil).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_WhenUserNotFoundAfterAuthentication_ShouldThrowException() {
        // Given
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUserResponse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        UserResponse response = authService.getCurrentUser();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getRole()).isEqualTo(RoleType.EMPLOYEE);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).findByEmail("john@example.com");

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("john@example.com");

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithDepartment_ShouldIncludeDepartmentInfo() {
        // Given
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("Engineering");
        department.setCode("ENG");

        user.setDepartment(department);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        UserResponse response = authService.getCurrentUser();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDepartmentId()).isEqualTo(department.getId());
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");
        assertThat(response.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findByEmail("john@example.com");

        // Clean up
        SecurityContextHolder.clearContext();
    }
}
