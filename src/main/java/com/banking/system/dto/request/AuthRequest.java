package com.banking.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

public class AuthRequest {

    @Data
    public static class Login {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class Register {
        @NotBlank @Size(min = 3, max = 50)
        private String username;
        @NotBlank @Email(message = "Invalid email format")
        private String email;
        @NotBlank @Size(min = 8, max = 120)
        private String password;
        @NotBlank @Size(max = 100)
        private String fullName;
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
        private String phoneNumber;
        private Set<String> roles;
    }

    @Data
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }

    @Data
    public static class ChangePassword {
        @NotBlank
        private String currentPassword;
        @NotBlank @Size(min = 8)
        private String newPassword;
    }
}