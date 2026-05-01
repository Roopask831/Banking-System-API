package com.banking.system.service.impl;

import com.banking.system.dto.request.AccountRequest;
import com.banking.system.dto.response.ApiResponse;
import com.banking.system.entity.Account;
import com.banking.system.entity.User;
import com.banking.system.exception.ResourceNotFoundException;
import com.banking.system.exception.UnauthorizedAccountAccessException;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.UserRepository;
import com.banking.system.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public ApiResponse.AccountResponse createAccount(String username, AccountRequest.Create request) {
        User user = findUserByUsername(username);
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .balance(BigDecimal.ZERO).status(Account.AccountStatus.ACTIVE).user(user).build();
        Account saved = accountRepository.save(account);
        log.info("Created {} account {} for user '{}'", saved.getAccountType(), saved.getAccountNumber(), username);
        return mapToAccountResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.AccountResponse getAccountByNumber(String accountNumber, String username) {
        Account account = findAccountByNumber(accountNumber);
        verifyOwnership(account, username);
        return mapToAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.AccountResponse getAccountByNumberAdmin(String accountNumber) {
        return mapToAccountResponse(findAccountByNumber(accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiResponse.AccountResponse> getUserAccounts(String username) {
        User user = findUserByUsername(username);
        return accountRepository.findByUserId(user.getId()).stream()
                .map(this::mapToAccountResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.PagedResponse<ApiResponse.AccountResponse> getUserAccountsPaged(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        Page<Account> page = accountRepository.findByUserId(user.getId(), pageable);
        return ApiResponse.PagedResponse.<ApiResponse.AccountResponse>builder()
                .content(page.getContent().stream().map(this::mapToAccountResponse).collect(Collectors.toList()))
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
    }

    @Override
    @Transactional
    public ApiResponse.AccountResponse updateAccountStatus(String accountNumber,
                                                           AccountRequest.UpdateStatus request, String username) {
        Account account = findAccountByNumber(accountNumber);
        verifyOwnership(account, username);
        account.setStatus(request.getStatus());
        return mapToAccountResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public ApiResponse.MessageResponse closeAccount(String accountNumber, String username) {
        Account account = findAccountByNumber(accountNumber);
        verifyOwnership(account, username);
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0)
            throw new IllegalStateException("Cannot close account with remaining balance. Please withdraw all funds first.");
        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);
        return ApiResponse.MessageResponse.success("Account closed successfully");
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            String digits = String.format("%012d", Math.abs(secureRandom.nextLong() % 1_000_000_000_000L));
            accountNumber = "ACC" + digits;
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }

    private void verifyOwnership(Account account, String username) {
        if (!account.getUser().getUsername().equals(username))
            throw new UnauthorizedAccountAccessException();
    }

    public ApiResponse.AccountResponse mapToAccountResponse(Account account) {
        return ApiResponse.AccountResponse.builder()
                .id(account.getId()).accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType()).balance(account.getBalance())
                .currency(account.getCurrency()).status(account.getStatus())
                .ownerUsername(account.getUser().getUsername())
                .createdAt(account.getCreatedAt()).updatedAt(account.getUpdatedAt()).build();
    }
}