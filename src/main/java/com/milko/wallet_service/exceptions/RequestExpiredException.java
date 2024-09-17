package com.milko.wallet_service.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RequestExpiredException extends RuntimeException{
    private String message;
    private LocalDateTime time;

    public RequestExpiredException(String message, LocalDateTime time) {
        this.message = message;
        this.time = time;
    }
}
