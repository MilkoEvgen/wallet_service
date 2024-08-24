package com.milko.wallet_service.exceptions;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException() {
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
