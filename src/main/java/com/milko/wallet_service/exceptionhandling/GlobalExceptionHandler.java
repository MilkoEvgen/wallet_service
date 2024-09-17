package com.milko.wallet_service.exceptionhandling;

import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.exceptions.RequestExpiredException;
import com.milko.wallet_service.exceptions.TransactionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtExceptions(Exception ex) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("cause", ex.getCause());
        return ErrorResponse.builder()
                .type("UnhandledException")
                .message("An unexpected error occurred: " + ex.getMessage())
                .time(LocalDateTime.now())
                .extra(extra)
                .build();
    }

    @ExceptionHandler(LowBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleLowBalanceException(LowBalanceException ex) {
        return ErrorResponse.builder()
                .type(LowBalanceException.class.getSimpleName())
                .message(ex.getMessage())
                .time(ex.getTime())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        return ErrorResponse.builder()
                .type(NotFoundException.class.getSimpleName())
                .message(ex.getMessage())
                .time(ex.getTime())
                .build();
    }

    @ExceptionHandler(RequestExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ErrorResponse handleRequestExpiredException(RequestExpiredException ex) {
        return ErrorResponse.builder()
                .type(RequestExpiredException.class.getSimpleName())
                .message(ex.getMessage())
                .time(ex.getTime())
                .build();
    }

    @ExceptionHandler(TransactionFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleTransactionFailedException(TransactionFailedException ex) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("cause", ex.getCause());
        return ErrorResponse.builder()
                .type(TransactionFailedException.class.getSimpleName())
                .message(ex.getMessage())
                .time(ex.getTime())
                .extra(extra)
                .build();
    }
}
