package com.milko.wallet_service.exceptions;

public class RequestExpiredException extends RuntimeException{
    public RequestExpiredException() {
    }
    public RequestExpiredException(String message) {
        super(message);
    }
}
