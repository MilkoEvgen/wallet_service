package com.milko.wallet_service.exceptions;

public class LowBalanceException extends RuntimeException{
    public LowBalanceException() {
    }

    public LowBalanceException(String message) {
        super(message);
    }
}
