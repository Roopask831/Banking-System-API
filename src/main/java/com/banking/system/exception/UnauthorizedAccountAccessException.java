package com.banking.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException() {
        super("You do not have permission to access this account");
    }
}