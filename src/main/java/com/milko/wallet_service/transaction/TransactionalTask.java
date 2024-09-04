package com.milko.wallet_service.transaction;

@FunctionalInterface
public interface TransactionalTask<T> {
    T execute(TransactionContext context);
}
