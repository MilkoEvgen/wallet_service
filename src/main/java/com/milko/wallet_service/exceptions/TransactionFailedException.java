package com.milko.wallet_service.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionFailedException extends RuntimeException{
    private String message;
    private LocalDateTime time;
    public TransactionFailedException(String message, Throwable cause, LocalDateTime time) {
        super(cause);
        this.message = message;
        this.time = time;
    }
}
