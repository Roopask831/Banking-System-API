package com.banking.system.controller;

import com.banking.system.dto.request.AccountRequest;
import com.banking.system.dto.response.ApiResponse;
import com.banking.system.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<ApiResponse.AccountResponse> createAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(userDetails.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "Get all my accounts")
    public ResponseEntity<List<ApiResponse.AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getUserAccounts(userDetails.getUsername()));
    }

    @GetMapping("/paged")
    @Operation(summary = "Get accounts paginated")
    public ResponseEntity<ApiResponse.PagedResponse<ApiResponse.AccountResponse>> getMyAccountsPaged(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(accountService.getUserAccountsPaged(userDetails.getUsername(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by number")
    public ResponseEntity<ApiResponse.AccountResponse> getAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber, userDetails.getUsername()));
    }

    @PatchMapping("/{accountNumber}/status")
    @Operation(summary = "Update account status")
    public ResponseEntity<ApiResponse.AccountResponse> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRequest.UpdateStatus request) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountNumber, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Close an account")
    public ResponseEntity<ApiResponse.MessageResponse> closeAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.closeAccount(accountNumber, userDetails.getUsername()));
    }
}