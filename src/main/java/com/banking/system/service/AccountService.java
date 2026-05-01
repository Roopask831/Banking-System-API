package com.banking.system.service;

import com.banking.system.dto.request.AccountRequest;
import com.banking.system.dto.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AccountService {
    ApiResponse.AccountResponse createAccount(String username, AccountRequest.Create request);
    ApiResponse.AccountResponse getAccountByNumber(String accountNumber, String username);
    ApiResponse.AccountResponse getAccountByNumberAdmin(String accountNumber);
    List<ApiResponse.AccountResponse> getUserAccounts(String username);
    ApiResponse.PagedResponse<ApiResponse.AccountResponse> getUserAccountsPaged(String username, Pageable pageable);
    ApiResponse.AccountResponse updateAccountStatus(String accountNumber, AccountRequest.UpdateStatus request, String username);
    ApiResponse.MessageResponse closeAccount(String accountNumber, String username);
}