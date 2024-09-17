package com.milko.wallet_service.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LowBalanceException extends RuntimeException{
    private String message;
    private LocalDateTime time;

    public LowBalanceException(String message, LocalDateTime time) {
        this.message = message;
        this.time = time;
    }
}
