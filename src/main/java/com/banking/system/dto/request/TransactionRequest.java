package com.banking.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

public class TransactionRequest {

    @Data
    public static class Deposit {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        @NotNull @DecimalMin(value = "0.01")
        private BigDecimal amount;
        @Size(max = 255)
        private String description;
    }

    @Data
    public static class Withdrawal {
        @NotBlank(message = "Account number is required")
        private String accountNumber;
        @NotNull @DecimalMin(value = "0.01")
        private BigDecimal amount;
        @Size(max = 255)
        private String description;
    }

    @Data
    public static class Transfer {
        @NotBlank(message = "Source account number is required")
        private String sourceAccountNumber;
        @NotBlank(message = "Destination account number is required")
        private String destinationAccountNumber;
        @NotNull @DecimalMin(value = "0.01")
        private BigDecimal amount;
        @Size(max = 255)
        private String description;
    }
}