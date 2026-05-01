package com.banking.system.controller;

import com.banking.system.dto.response.ApiResponse;
import com.banking.system.service.AccountService;
import com.banking.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse.PagedResponse<ApiResponse.UserProfileResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse.UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Enable or disable a user")
    public ResponseEntity<ApiResponse.MessageResponse> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse.MessageResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/accounts/{accountNumber}")
    @Operation(summary = "Get any account (admin)")
    public ResponseEntity<ApiResponse.AccountResponse> getAnyAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumberAdmin(accountNumber));
    }
}