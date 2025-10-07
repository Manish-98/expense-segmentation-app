package com.expense.segmentation.controller;

import com.expense.segmentation.dto.UpdateUserRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Admin only - retrieves all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/department")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get users in manager's department",
            description = "Manager only - retrieves users in the same department")
    public ResponseEntity<List<UserResponse>> getUsersInDepartment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User manager =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Manager not found"));

        List<UserResponse> users = userService.getUsersByDepartment(manager.getId());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user",
            description = "Admin only - updates user's role or department")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deactivate user",
            description = "Admin only - sets user status to INACTIVE")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
