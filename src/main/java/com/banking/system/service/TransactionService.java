package com.banking.system.service;

import com.banking.system.dto.request.TransactionRequest;
import com.banking.system.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface TransactionService {
    ApiResponse.TransactionResponse deposit(TransactionRequest.Deposit request, String username);
    ApiResponse.TransactionResponse withdraw(TransactionRequest.Withdrawal request, String username);
    ApiResponse.TransactionResponse transfer(TransactionRequest.Transfer request, String username);
    ApiResponse.TransactionResponse getTransactionByReference(String referenceId, String username);
    ApiResponse.PagedResponse<ApiResponse.TransactionResponse> getAccountTransactions(
            String accountNumber, String username, Pageable pageable);
    ApiResponse.PagedResponse<ApiResponse.TransactionResponse> getAccountTransactionsByDateRange(
            String accountNumber, String username,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}