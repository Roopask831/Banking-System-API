package com.banking.system.dto.request;

import com.banking.system.entity.Account;
import jakarta.validation.constraints.*;
import lombok.Data;

public class AccountRequest {

    @Data
    public static class Create {
        @NotNull(message = "Account type is required")
        private Account.AccountType accountType;
        @Size(max = 3)
        private String currency = "USD";
    }

    @Data
    public static class UpdateStatus {
        @NotNull
        private Account.AccountStatus status;
        @Size(max = 255)
        private String reason;
    }
}