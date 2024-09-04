package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.model.Transaction;

import java.util.UUID;

public interface TransactionService {
    TransactionOutputDto create(Transaction transaction);
    TransactionOutputDto findById(UUID transactionId, UUID profileId);
    TransactionOutputDto complete(UUID transactionId, UUID profileId);
}
