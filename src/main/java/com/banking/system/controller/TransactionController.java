package com.banking.system.controller;

import com.banking.system.dto.request.TransactionRequest;
import com.banking.system.dto.response.ApiResponse;
import com.banking.system.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds")
    public ResponseEntity<ApiResponse.TransactionResponse> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest.Deposit request) {
        return ResponseEntity.ok(transactionService.deposit(request, userDetails.getUsername()));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds")
    public ResponseEntity<ApiResponse.TransactionResponse> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest.Withdrawal request) {
        return ResponseEntity.ok(transactionService.withdraw(request, userDetails.getUsername()));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer between accounts")
    public ResponseEntity<ApiResponse.TransactionResponse> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest.Transfer request) {
        return ResponseEntity.ok(transactionService.transfer(request, userDetails.getUsername()));
    }

    @GetMapping("/{referenceId}")
    @Operation(summary = "Get transaction by reference")
    public ResponseEntity<ApiResponse.TransactionResponse> getTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String referenceId) {
        return ResponseEntity.ok(transactionService.getTransactionByReference(referenceId, userDetails.getUsername()));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transaction history")
    public ResponseEntity<ApiResponse.PagedResponse<ApiResponse.TransactionResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountNumber,
                userDetails.getUsername(), PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/account/{accountNumber}/history")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<ApiResponse.PagedResponse<ApiResponse.TransactionResponse>> getHistoryByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getAccountTransactionsByDateRange(
                accountNumber, userDetails.getUsername(), startDate, endDate,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}