package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction create(Transaction transaction, DataSource dataSource);
    Transaction updateStatus(UUID transactionId, TransactionStatus status, DataSource dataSource);
    Transaction findById(UUID transactionId, DataSource dataSource);
}
