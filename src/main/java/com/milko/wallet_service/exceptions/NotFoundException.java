package com.milko.wallet_service.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotFoundException extends RuntimeException{
    private String message;
    private LocalDateTime time;
    public NotFoundException(String message, LocalDateTime time) {
        this.message = message;
        this.time = time;
    }
}
