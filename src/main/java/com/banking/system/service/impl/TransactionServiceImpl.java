package com.banking.system.service.impl;

import com.banking.system.dto.request.TransactionRequest;
import com.banking.system.dto.response.ApiResponse;
import com.banking.system.entity.Account;
import com.banking.system.entity.Transaction;
import com.banking.system.exception.*;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.TransactionRepository;
import com.banking.system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public ApiResponse.TransactionResponse deposit(TransactionRequest.Deposit request, String username) {
        Account account = getAccountWithLock(request.getAccountNumber());
        verifyOwnership(account, username);
        verifyAccountActive(account);
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        Transaction txn = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.DEPOSIT).amount(request.getAmount())
                .currency(account.getCurrency()).status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription()).destinationAccount(account)
                .balanceAfterTransaction(account.getBalance()).build();
        return mapToTransactionResponse(transactionRepository.save(txn));
    }

    @Override
    @Transactional
    public ApiResponse.TransactionResponse withdraw(TransactionRequest.Withdrawal request, String username) {
        Account account = getAccountWithLock(request.getAccountNumber());
        verifyOwnership(account, username);
        verifyAccountActive(account);
        if (account.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientFundsException(request.getAccountNumber());
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        Transaction txn = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.WITHDRAWAL).amount(request.getAmount())
                .currency(account.getCurrency()).status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription()).sourceAccount(account)
                .balanceAfterTransaction(account.getBalance()).build();
        return mapToTransactionResponse(transactionRepository.save(txn));
    }

    @Override
    @Transactional
    public ApiResponse.TransactionResponse transfer(TransactionRequest.Transfer request, String username) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber()))
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        Account source, destination;
        if (request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0) {
            source = getAccountWithLock(request.getSourceAccountNumber());
            destination = getAccountWithLock(request.getDestinationAccountNumber());
        } else {
            destination = getAccountWithLock(request.getDestinationAccountNumber());
            source = getAccountWithLock(request.getSourceAccountNumber());
        }
        verifyOwnership(source, username);
        verifyAccountActive(source);
        verifyAccountActive(destination);
        if (source.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientFundsException(request.getSourceAccountNumber());
        source.setBalance(source.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepository.save(source);
        accountRepository.save(destination);
        Transaction txn = Transaction.builder()
                .referenceId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.TRANSFER).amount(request.getAmount())
                .currency(source.getCurrency()).status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription()).sourceAccount(source)
                .destinationAccount(destination).balanceAfterTransaction(source.getBalance()).build();
        return mapToTransactionResponse(transactionRepository.save(txn));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.TransactionResponse getTransactionByReference(String referenceId, String username) {
        Transaction txn = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "referenceId", referenceId));
        boolean isOwner = (txn.getSourceAccount() != null &&
                txn.getSourceAccount().getUser().getUsername().equals(username)) ||
                (txn.getDestinationAccount() != null &&
                        txn.getDestinationAccount().getUser().getUsername().equals(username));
        if (!isOwner) throw new UnauthorizedAccountAccessException();
        return mapToTransactionResponse(txn);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.PagedResponse<ApiResponse.TransactionResponse> getAccountTransactions(
            String accountNumber, String username, Pageable pageable) {
        Account account = findAccountByNumber(accountNumber);
        verifyOwnership(account, username);
        return buildPagedResponse(transactionRepository.findAllByAccountId(account.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse.PagedResponse<ApiResponse.TransactionResponse> getAccountTransactionsByDateRange(
            String accountNumber, String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Account account = findAccountByNumber(accountNumber);
        verifyOwnership(account, username);
        return buildPagedResponse(transactionRepository.findByAccountIdAndDateRange(
                account.getId(), startDate, endDate, pageable));
    }

    private Account getAccountWithLock(String accountNumber) {
        return accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }

    private void verifyOwnership(Account account, String username) {
        if (!account.getUser().getUsername().equals(username)) throw new UnauthorizedAccountAccessException();
    }

    private void verifyAccountActive(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) throw new AccountNotActiveException(account.getAccountNumber());
    }

    private ApiResponse.PagedResponse<ApiResponse.TransactionResponse> buildPagedResponse(Page<Transaction> page) {
        return ApiResponse.PagedResponse.<ApiResponse.TransactionResponse>builder()
                .content(page.getContent().stream().map(this::mapToTransactionResponse).collect(Collectors.toList()))
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
    }

    private ApiResponse.TransactionResponse mapToTransactionResponse(Transaction txn) {
        return ApiResponse.TransactionResponse.builder()
                .id(txn.getId()).referenceId(txn.getReferenceId()).type(txn.getType())
                .amount(txn.getAmount()).currency(txn.getCurrency()).status(txn.getStatus())
                .description(txn.getDescription())
                .sourceAccountNumber(txn.getSourceAccount() != null ? txn.getSourceAccount().getAccountNumber() : null)
                .destinationAccountNumber(txn.getDestinationAccount() != null ? txn.getDestinationAccount().getAccountNumber() : null)
                .balanceAfterTransaction(txn.getBalanceAfterTransaction()).createdAt(txn.getCreatedAt()).build();
    }
}