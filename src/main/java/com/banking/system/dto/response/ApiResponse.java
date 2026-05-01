package com.banking.system.dto.response;

import com.banking.system.entity.Account;
import com.banking.system.entity.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApiResponse {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class JwtResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private Long userId;
        private String username;
        private String email;
        private List<String> roles;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
        private Object data;

        public static MessageResponse success(String message) {
            return MessageResponse.builder().message(message).success(true).build();
        }
        public static MessageResponse success(String message, Object data) {
            return MessageResponse.builder().message(message).success(true).data(data).build();
        }
        public static MessageResponse error(String message) {
            return MessageResponse.builder().message(message).success(false).build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AccountResponse {
        private Long id;
        private String accountNumber;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private String currency;
        private Account.AccountStatus status;
        private String ownerUsername;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AccountSummary {
        private String accountNumber;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private String currency;
        private Account.AccountStatus status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private String referenceId;
        private Transaction.TransactionType type;
        private BigDecimal amount;
        private String currency;
        private Transaction.TransactionStatus status;
        private String description;
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private BigDecimal balanceAfterTransaction;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phoneNumber;
        private boolean enabled;
        private List<String> roles;
        private List<AccountSummary> accounts;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PagedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}