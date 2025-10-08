package com.expense.segmentation.service;

import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.AuthResponse;
import com.expense.segmentation.dto.LoginRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.UserMapper;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Registration failed: email {} already exists", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get default EMPLOYEE role
        Role employeeRole =
                roleRepository
                        .findByName(RoleType.EMPLOYEE)
                        .orElseThrow(
                                () -> {
                                    log.error("EMPLOYEE role not found in database");
                                    return new ResourceNotFoundException(
                                            "Role", "name", RoleType.EMPLOYEE.toString());
                                });

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(employeeRole);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);
        log.info("Successfully registered new user: {} with id: {}", user.getEmail(), user.getId());

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);

        // Create response
        UserResponse userResponse = userMapper.toResponse(user);
        return new AuthResponse(token, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        // Authenticate user
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        // Get user details
        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "User not found after successful authentication: {}",
                                            request.getEmail());
                                    return new ResourceNotFoundException(
                                            "User", "email", request.getEmail());
                                });

        log.info("User {} logged in successfully", request.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);
        return new AuthResponse(token, userResponse);
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Fetching current user details for: {}", email);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "Authenticated user not found in database: {}", email);
                                    return new ResourceNotFoundException("User", "email", email);
                                });

        return userMapper.toResponse(user);
    }
}
